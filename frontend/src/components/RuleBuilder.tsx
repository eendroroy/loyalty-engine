import { useState, useEffect } from 'react';
import {
  Box, Card, CardContent, Typography, Button, Select, MenuItem,
  TextField, FormControl, InputLabel, IconButton, Tooltip,
  Chip, Divider, Alert, Paper, Stack,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import ContentCopyRoundedIcon from '@mui/icons-material/ContentCopyRounded';
import GroupWorkRoundedIcon from '@mui/icons-material/GroupWorkRounded';
import UnfoldMoreRoundedIcon from '@mui/icons-material/UnfoldMoreRounded';
import UnfoldLessRoundedIcon from '@mui/icons-material/UnfoldLessRounded';
import { getVouchers } from '../api/vouchers';
import type { Metadata, FieldDataType, RewardType, Voucher } from '../types';

type ComparisonOperator = '>' | '<' | '>=' | '<=' | '=' | '!=' | 'CONTAINS' | 'STARTS_WITH' | 'ENDS_WITH';
type LogicalOperator = 'AND' | 'OR';

interface Condition {
  id: string;
  fieldPath: string;
  operator: ComparisonOperator;
  value: string;
  fieldType?: FieldDataType;
}

interface ConditionGroup {
  id: string;
  logicalOperator: LogicalOperator;
  conditions: Condition[];
  expanded: boolean;
}

interface Reward {
  type: RewardType;
  value: string;
}

interface Props {
  metadata: Metadata | null;
  initialExpression?: string;
  onExpressionChange: (expression: string) => void;
}

// Operators based on field data type
const getOperatorsForType = (dataType: FieldDataType): ComparisonOperator[] => {
  switch (dataType) {
    case 'STRING':
      return ['=', '!=', 'CONTAINS', 'STARTS_WITH', 'ENDS_WITH'];
    case 'INTEGER':
    case 'DECIMAL':
      return ['>', '<', '>=', '<=', '=', '!='];
    case 'DATE':
      return ['>', '<', '>=', '<=', '=', '!='];
    case 'BOOLEAN':
      return ['=', '!='];
    default:
      return ['=', '!='];
  }
};

// Get all field paths from metadata
const getAllFieldPaths = (metadata: Metadata | null): Array<{ path: string; type: FieldDataType; description?: string }> => {
  if (!metadata) return [];

  return metadata.dataSources.flatMap(ds =>
    ds.fields.map(field => ({
      path: `${ds.name}.${field.alias}`,
      type: field.dataType,
      description: field.description,
    }))
  );
};

// Generate unique ID
const generateId = () => Math.random().toString(36).substr(2, 9);

export default function RuleBuilder({ metadata, initialExpression, onExpressionChange }: Props) {
  const [conditionGroups, setConditionGroups] = useState<ConditionGroup[]>([]);
  const [reward, setReward] = useState<Reward>({ type: 'POINT', value: '' });
  const [copied, setCopied] = useState(false);
  const [vouchers, setVouchers] = useState<Voucher[]>([]);

  const fieldPaths = getAllFieldPaths(metadata);

  // Load vouchers for dropdown
  useEffect(() => {
    getVouchers()
      .then(response => setVouchers(response.data.filter(v => v.active && !v.archived)))
      .catch(() => setVouchers([]));
  }, []);

  // Initialize with empty condition group
  useEffect(() => {
    if (conditionGroups.length === 0) {
      setConditionGroups([{
        id: generateId(),
        logicalOperator: 'AND',
        expanded: true,
        conditions: [{
          id: generateId(),
          fieldPath: '',
          operator: '=',
          value: '',
        }]
      }]);
    }
  }, [conditionGroups.length]);

  // Generate rule expression from visual components
  const generateExpression = (): string => {
    if (conditionGroups.length === 0 || !reward.value) return '';

    // Build condition part
    const conditionParts = conditionGroups.map(group => {
      const groupConditions = group.conditions
        .filter(c => c.fieldPath && c.value)
        .map(condition => {
          const { fieldPath, operator, value, fieldType } = condition;

          // Format value based on field type
          let formattedValue = value;
          if (fieldType === 'STRING' && !['CONTAINS', 'STARTS_WITH', 'ENDS_WITH'].includes(operator)) {
            formattedValue = `"${value}"`;
          } else if (fieldType === 'BOOLEAN') {
            formattedValue = value.toLowerCase() === 'true' ? 'true' : 'false';
          }

          return `${fieldPath} ${operator} ${formattedValue}`;
        });

      if (groupConditions.length === 0) return '';
      if (groupConditions.length === 1) return groupConditions[0];

      return `(${groupConditions.join(` ${group.logicalOperator} `)})`;
    }).filter(Boolean);

    const conditionExpression = conditionParts.length > 1
      ? conditionParts.join(' OR ')
      : conditionParts[0];

    // Build reward part
    const rewardExpression = reward.type === 'POINT'
      ? `Point(${reward.value})`
      : `Voucher(${reward.value})`;

    return `WHEN ${conditionExpression} THEN ${rewardExpression}`;
  };

  // Update expression when components change
  useEffect(() => {
    const expression = generateExpression();
    onExpressionChange(expression);
  }, [conditionGroups, reward, onExpressionChange]);

  // Add condition to a group
  const addCondition = (groupId: string) => {
    setConditionGroups(prev => prev.map(group =>
      group.id === groupId
        ? {
            ...group,
            conditions: [...group.conditions, {
              id: generateId(),
              fieldPath: '',
              operator: '=',
              value: '',
            }]
          }
        : group
    ));
  };

  // Remove condition from a group
  const removeCondition = (groupId: string, conditionId: string) => {
    setConditionGroups(prev => prev.map(group =>
      group.id === groupId
        ? {
            ...group,
            conditions: group.conditions.filter(c => c.id !== conditionId)
          }
        : group
    ).filter(group => group.conditions.length > 0)); // Remove empty groups
  };

  // Update condition
  const updateCondition = (groupId: string, conditionId: string, updates: Partial<Condition>) => {
    setConditionGroups(prev => prev.map(group =>
      group.id === groupId
        ? {
            ...group,
            conditions: group.conditions.map(condition =>
              condition.id === conditionId
                ? { ...condition, ...updates }
                : condition
            )
          }
        : group
    ));
  };

  // Add condition group
  const addConditionGroup = () => {
    setConditionGroups(prev => [...prev, {
      id: generateId(),
      logicalOperator: 'AND',
      expanded: true,
      conditions: [{
        id: generateId(),
        fieldPath: '',
        operator: '=',
        value: '',
      }]
    }]);
  };

  // Remove condition group
  const removeConditionGroup = (groupId: string) => {
    setConditionGroups(prev => prev.filter(group => group.id !== groupId));
  };

  // Update group logical operator
  const updateGroupOperator = (groupId: string, operator: LogicalOperator) => {
    setConditionGroups(prev => prev.map(group =>
      group.id === groupId ? { ...group, logicalOperator: operator } : group
    ));
  };

  // Toggle group expansion
  const toggleGroupExpansion = (groupId: string) => {
    setConditionGroups(prev => prev.map(group =>
      group.id === groupId ? { ...group, expanded: !group.expanded } : group
    ));
  };

  // Copy expression to clipboard
  const handleCopyExpression = () => {
    const expression = generateExpression();
    navigator.clipboard.writeText(expression).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const expression = generateExpression();
  const hasValidConditions = conditionGroups.some(group =>
    group.conditions.some(c => c.fieldPath && c.value)
  );

  return (
    <Card variant="outlined">
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
          🔧 Visual Rule Builder
          <Tooltip title={copied ? "Copied!" : "Copy expression"}>
            <IconButton size="small" onClick={handleCopyExpression} disabled={!expression}>
              <ContentCopyRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Typography>

        {/* Generated Expression Preview */}
        <Paper sx={{ p: 2, mb: 3, bgcolor: 'grey.50', border: '1px solid', borderColor: 'divider' }}>
          <Typography variant="subtitle2" sx={{ mb: 1, color: 'text.secondary' }}>
            Generated Rule Expression:
          </Typography>
          <Typography
            variant="body1"
            sx={{
              fontFamily: 'monospace',
              wordBreak: 'break-all',
              minHeight: '1.5em',
              color: expression ? 'text.primary' : 'text.disabled',
              fontWeight: expression ? 500 : 400,
            }}
          >
            {expression || 'Configure conditions and reward to see the rule expression'}
          </Typography>
        </Paper>

        {/* WHEN Section */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 2, color: 'primary.main', fontWeight: 600 }}>
            WHEN
          </Typography>

          <Stack spacing={2}>
            {conditionGroups.map((group, groupIndex) => (
              <Box key={group.id}>
                {groupIndex > 0 && (
                  <Box sx={{ textAlign: 'center', my: 2 }}>
                    <Chip
                      label="OR"
                      color="primary"
                      variant="outlined"
                      sx={{ fontWeight: 600, fontSize: '0.875rem' }}
                    />
                  </Box>
                )}

                <Card variant="outlined" sx={{ backgroundColor: 'background.default' }}>
                  <CardContent>
                    {/* Group Header */}
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <GroupWorkRoundedIcon fontSize="small" color="action" />
                        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                          Condition Group {groupIndex + 1}
                        </Typography>
                        {group.conditions.length > 1 && (
                          <Chip
                            size="small"
                            label={group.logicalOperator}
                            color="secondary"
                            variant="outlined"
                          />
                        )}
                      </Box>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        <IconButton
                          size="small"
                          onClick={() => toggleGroupExpansion(group.id)}
                        >
                          {group.expanded ? <UnfoldLessRoundedIcon /> : <UnfoldMoreRoundedIcon />}
                        </IconButton>
                        {conditionGroups.length > 1 && (
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => removeConditionGroup(group.id)}
                          >
                            <DeleteRoundedIcon />
                          </IconButton>
                        )}
                      </Box>
                    </Box>

                    {group.expanded && (
                      <>
                        {/* Group Logic Selector */}
                        {group.conditions.length > 1 && (
                          <Box sx={{ mb: 2 }}>
                            <FormControl size="small">
                              <InputLabel>Group Logic</InputLabel>
                              <Select
                                value={group.logicalOperator}
                                label="Group Logic"
                                onChange={(e) => updateGroupOperator(group.id, e.target.value as LogicalOperator)}
                              >
                                <MenuItem value="AND">AND - All conditions must be true</MenuItem>
                                <MenuItem value="OR">OR - Any condition can be true</MenuItem>
                              </Select>
                            </FormControl>
                          </Box>
                        )}

                        {/* Conditions */}
                        <Stack spacing={2}>
                          {group.conditions.map((condition, conditionIndex) => (
                            <Box key={condition.id}>
                              {conditionIndex > 0 && (
                                <Box sx={{ textAlign: 'center', my: 1 }}>
                                  <Chip
                                    size="small"
                                    label={group.logicalOperator}
                                    color="secondary"
                                    variant="filled"
                                    sx={{ fontSize: '0.75rem' }}
                                  />
                                </Box>
                              )}

                              <Grid container spacing={2} alignItems="center">
                                {/* Field Selection */}
                                <Grid size={{ xs: 12, sm: 4 }}>
                                  <FormControl fullWidth size="small">
                                    <InputLabel>Property</InputLabel>
                                    <Select
                                      value={condition.fieldPath}
                                      label="Property"
                                      onChange={(e) => {
                                        const selectedPath = e.target.value;
                                        const field = fieldPaths.find(f => f.path === selectedPath);
                                        updateCondition(group.id, condition.id, {
                                          fieldPath: selectedPath,
                                          fieldType: field?.type,
                                          operator: field ? getOperatorsForType(field.type)[0] : '='
                                        });
                                      }}
                                    >
                                      {fieldPaths.map(field => (
                                        <MenuItem key={field.path} value={field.path}>
                                          <Box>
                                            <Typography variant="body2">{field.path}</Typography>
                                            <Typography variant="caption" color="text.secondary">
                                              {field.type}{field.description ? ` • ${field.description}` : ''}
                                            </Typography>
                                          </Box>
                                        </MenuItem>
                                      ))}
                                    </Select>
                                  </FormControl>
                                </Grid>

                                {/* Operator Selection */}
                                <Grid size={{ xs: 12, sm: 3 }}>
                                  <FormControl fullWidth size="small">
                                    <InputLabel>Operator</InputLabel>
                                    <Select
                                      value={condition.operator}
                                      label="Operator"
                                      onChange={(e) => updateCondition(group.id, condition.id, { operator: e.target.value as ComparisonOperator })}
                                    >
                                      {getOperatorsForType(condition.fieldType || 'STRING').map(op => (
                                        <MenuItem key={op} value={op}>{op}</MenuItem>
                                      ))}
                                    </Select>
                                  </FormControl>
                                </Grid>

                                {/* Value Input */}
                                <Grid size={{ xs: 12, sm: 4 }}>
                                  <TextField
                                    fullWidth
                                    size="small"
                                    label="Value"
                                    value={condition.value}
                                    onChange={(e) => updateCondition(group.id, condition.id, { value: e.target.value })}
                                    type={condition.fieldType === 'INTEGER' || condition.fieldType === 'DECIMAL' ? 'number' :
                                          condition.fieldType === 'DATE' ? 'date' : 'text'}
                                    placeholder={
                                      condition.fieldType === 'BOOLEAN' ? 'true or false' :
                                      condition.fieldType === 'DATE' ? 'YYYY-MM-DD' :
                                      condition.fieldType === 'STRING' && ['CONTAINS', 'STARTS_WITH', 'ENDS_WITH'].includes(condition.operator) ? 'Text to search' :
                                      'Enter value'
                                    }
                                  />
                                </Grid>

                                {/* Actions */}
                                <Grid size={{ xs: 12, sm: 1 }}>
                                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                                    <Tooltip title="Add condition">
                                      <IconButton
                                        size="small"
                                        color="primary"
                                        onClick={() => addCondition(group.id)}
                                      >
                                        <AddRoundedIcon fontSize="small" />
                                      </IconButton>
                                    </Tooltip>
                                    {group.conditions.length > 1 && (
                                      <Tooltip title="Remove condition">
                                        <IconButton
                                          size="small"
                                          color="error"
                                          onClick={() => removeCondition(group.id, condition.id)}
                                        >
                                          <DeleteRoundedIcon fontSize="small" />
                                        </IconButton>
                                      </Tooltip>
                                    )}
                                  </Box>
                                </Grid>
                              </Grid>
                            </Box>
                          ))}
                        </Stack>
                      </>
                    )}
                  </CardContent>
                </Card>
              </Box>
            ))}
          </Stack>

          <Button
            variant="outlined"
            color="primary"
            startIcon={<AddRoundedIcon />}
            onClick={addConditionGroup}
            sx={{ mt: 2 }}
          >
            Add Condition Group (OR)
          </Button>
        </Box>

        <Divider sx={{ my: 3 }} />

        {/* THEN Section */}
        <Box>
          <Typography variant="h5" sx={{ mb: 2, color: 'secondary.main', fontWeight: 600 }}>
            THEN
          </Typography>

          <Card variant="outlined" sx={{ backgroundColor: 'background.default' }}>
            <CardContent>
              <Grid container spacing={2} alignItems="center">
                <Grid size={{ xs: 12, sm: 3 }}>
                  <FormControl fullWidth size="small">
                    <InputLabel>Reward Type</InputLabel>
                    <Select
                      value={reward.type}
                      label="Reward Type"
                      onChange={(e) => setReward(prev => ({ type: e.target.value as RewardType, value: '' }))}
                    >
                      <MenuItem value="POINT">
                        <Box>
                          <Typography variant="body2">Points</Typography>
                          <Typography variant="caption" color="text.secondary">Award loyalty points</Typography>
                        </Box>
                      </MenuItem>
                      <MenuItem value="VOUCHER">
                        <Box>
                          <Typography variant="body2">Voucher</Typography>
                          <Typography variant="caption" color="text.secondary">Award a voucher code</Typography>
                        </Box>
                      </MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid size={{ xs: 12, sm: 6 }}>
                  {reward.type === 'POINT' ? (
                    <TextField
                      fullWidth
                      size="small"
                      label="Points Amount"
                      value={reward.value}
                      onChange={(e) => setReward(prev => ({ ...prev, value: e.target.value }))}
                      type="number"
                      placeholder="Enter number of points to award"
                      inputProps={{ min: 0, step: 1 }}
                    />
                  ) : (
                    <FormControl fullWidth size="small">
                      <InputLabel>Voucher Code</InputLabel>
                      <Select
                        value={reward.value}
                        label="Voucher Code"
                        onChange={(e) => setReward(prev => ({ ...prev, value: e.target.value }))}
                      >
                        {vouchers.map(voucher => (
                          <MenuItem key={voucher.id} value={voucher.code}>
                            <Box>
                              <Typography variant="body2">{voucher.code}</Typography>
                              <Typography variant="caption" color="text.secondary">
                                {voucher.name} • {voucher.count - (voucher.instanceCount || 0)} remaining
                              </Typography>
                            </Box>
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )}
                </Grid>

                <Grid size={{ xs: 12, sm: 3 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {reward.type && reward.value && (
                      <Chip
                        label={reward.type === 'POINT' ? `${reward.value} Points` : `Voucher: ${reward.value}`}
                        color="secondary"
                        variant="filled"
                      />
                    )}
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Box>

        {/* Status Message */}
        <Box sx={{ mt: 3 }}>
          {!hasValidConditions || !reward.value ? (
            <Alert severity="info">
              💡 Complete at least one condition and configure the reward to generate a valid rule expression.
            </Alert>
          ) : (
            <Alert severity="success">
              ✅ Rule expression is ready! You can copy it or switch to text mode to make further adjustments.
            </Alert>
          )}
        </Box>
      </CardContent>
    </Card>
  );
}
