import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, MenuItem, Button,
  Divider, Alert, CircularProgress, Tab, Tabs, Typography,
  IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogActions,
  ToggleButtonGroup, ToggleButton,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon    from '@mui/icons-material/AddRounded';
import EditRoundedIcon   from '@mui/icons-material/EditRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import SaveRoundedIcon   from '@mui/icons-material/SaveRounded';
import CodeRoundedIcon   from '@mui/icons-material/CodeRounded';
import BuildRoundedIcon  from '@mui/icons-material/BuildRounded';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { type Dayjs } from 'dayjs';
import PageHeader    from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import FieldBrowser  from '../components/FieldBrowser';
import RuleBuilder   from '../components/RuleBuilder';
import {
  getRule, createRule, updateRule,
  getActions, createAction, updateAction, deleteAction, validateExpression,
} from '../api/rules';
import { getMetadata } from '../api/metadata';
import type { Rule, RuleAction, RuleStatus, RewardType, Metadata } from '../types';

const EMPTY_RULE: Omit<Rule, 'id' | 'actions' | 'lastRunAt' | 'createdAt' | 'updatedAt'> = {
  name: '', description: '', ruleExpression: '', status: 'DRAFT', priority: 0,
  activeFrom: '', activeTo: '', frequency: '',
};

const EMPTY_ACTION: Omit<RuleAction, 'id'> = {
  rewardType: 'POINT', rewardAmount: '', description: '',
};

export default function RuleForm() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [form, setForm]                         = useState(EMPTY_RULE);
  const [actions, setActions]                   = useState<RuleAction[]>([]);
  const [tab, setTab]                           = useState(0);
  const [loading, setLoading]                   = useState(isEdit);
  const [saving, setSaving]                     = useState(false);
  const [error, setError]                       = useState('');
  const [actionDialog, setActionDialog]         = useState(false);
  const [actionForm, setActionForm]             = useState<Omit<RuleAction, 'id'>>(EMPTY_ACTION);
  const [editActionId, setEditActionId]         = useState<number | null>(null);
  const [deleteActionId, setDeleteActionId]     = useState<number | null>(null);
  const [metadata, setMetadata]                 = useState<Metadata | null>(null);
  const [validating, setValidating]             = useState(false);
  const [validationResult, setValidationResult] = useState<{ valid: boolean; error?: string } | null>(null);
  const [ruleEditorMode, setRuleEditorMode]     = useState<'text' | 'visual'>('text');

  /** Ref to the rule-expression <textarea> for cursor-position insertion. */
  const expressionRef = useRef<HTMLTextAreaElement>(null);

  /** Debounced expression validation */
  useEffect(() => {
    const timer = setTimeout(() => {
      if (form.ruleExpression.trim()) {
        setValidating(true);
        validateExpression(form.ruleExpression)
          .then((r) => setValidationResult(r.data))
          .catch(() => setValidationResult({ valid: false, error: 'Validation request failed' }))
          .finally(() => setValidating(false));
      } else {
        setValidationResult(null);
      }
    }, 800);
    return () => clearTimeout(timer);
  }, [form.ruleExpression]);

  /** Insert a field alias at the current cursor position in the expression. */
  const handleInsertField = (alias: string) => {
    const el  = expressionRef.current;
    const pos = el?.selectionStart ?? form.ruleExpression.length;
    const cur = form.ruleExpression;

    // Try to find the source name from metadata to create proper source.field format
    let fieldRef = alias;
    if (metadata) {
      for (const source of metadata.dataSources) {
        const field = source.fields.find(f => f.alias === alias);
        if (field) {
          fieldRef = `${source.name}.${alias}`;
          break;
        }
      }
    }

    const next = cur.slice(0, pos) + fieldRef + cur.slice(pos);
    setForm((p) => ({ ...p, ruleExpression: next }));

    // Restore cursor just after the inserted text
    requestAnimationFrame(() => {
      if (el) {
        el.selectionStart = pos + fieldRef.length;
        el.selectionEnd   = pos + fieldRef.length;
        el.focus();
      }
    });
  };

  useEffect(() => {
    // Always load metadata for the field browser
    getMetadata().then((r) => setMetadata(r.data)).catch(() => undefined);

    if (!isEdit) return;
    getRule(Number(id))
      .then((r) => {
        const { actions: a, id: _id, lastRunAt: _l, createdAt: _c, updatedAt: _u, ...rest } = r.data;
        setForm({ ...EMPTY_RULE, ...rest });
        setActions(a ?? []);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((p) => ({ ...p, [k]: e.target.value }));

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        activeFrom:  form.activeFrom  || undefined,
        activeTo:    form.activeTo    || undefined,
        frequency:   form.frequency   || undefined,
        description: form.description || undefined,
      };
      if (isEdit) await updateRule(Number(id), payload as Parameters<typeof createRule>[0]);
      else        await createRule(payload  as Parameters<typeof createRule>[0]);
      navigate('/rules');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  const loadActions = () => getActions(Number(id)).then((r) => setActions(r.data));

  const openActionDialog = (a?: RuleAction) => {
    setActionForm(
      a ? { rewardType: a.rewardType, rewardAmount: a.rewardAmount, description: a.description }
        : EMPTY_ACTION,
    );
    setEditActionId(a?.id ?? null);
    setActionDialog(true);
  };

  const handleSaveAction = async () => {
    if (editActionId) await updateAction(Number(id), editActionId, actionForm);
    else              await createAction(Number(id), actionForm);
    setActionDialog(false);
    loadActions();
  };

  const handleDeleteAction = async () => {
    await deleteAction(Number(id), deleteActionId!);
    setDeleteActionId(null);
    loadActions();
  };

  const actionColumns: GridColDef<RuleAction>[] = [
    { field: 'rewardType',   headerName: 'Reward Type',  width: 140 },
    { field: 'rewardAmount', headerName: 'Amount/Value', width: 140 },
    { field: 'description',  headerName: 'Description',  flex: 1 },
    {
      field: 'rowActions', headerName: '', width: 100, sortable: false,
      renderCell: ({ row }) => (
        <Box>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => openActionDialog(row)}>
              <EditRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" color="error" onClick={() => setDeleteActionId(row.id!)}>
              <DeleteRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      <PageHeader
        title={isEdit ? 'Edit Rule' : 'New Rule'}
        crumbs={[{ label: 'Rules', to: '/rules' }, { label: isEdit ? 'Edit' : 'New' }]}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <Tabs
          value={tab}
          onChange={(_, v) => setTab(v)}
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab label="Details" />
          {isEdit && <Tab label={`Reward Actions (${actions.length})`} />}
        </Tabs>

        <CardContent sx={{ p: 3 }}>
          {tab === 0 && (
            <Grid container spacing={3}>
              {/* Row 1 — name / status / priority */}
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth label="Name" value={form.name} onChange={set('name')} required />
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  fullWidth select label="Status"
                  value={form.status}
                  onChange={set('status') as never}
                >
                  {(['DRAFT', 'ACTIVE', 'INACTIVE'] as RuleStatus[]).map((s) => (
                    <MenuItem key={s} value={s}>{s}</MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  fullWidth label="Priority" type="number"
                  value={form.priority}
                  onChange={(e) => setForm((p) => ({ ...p, priority: Number(e.target.value) }))}
                />
              </Grid>

              {/* Row 2 — description */}
              <Grid size={{ xs: 12 }}>
                <TextField
                  fullWidth multiline rows={2}
                  label="Description"
                  value={form.description ?? ''}
                  onChange={set('description')}
                />
              </Grid>

              {/* Row 3 — rule expression */}
              <Grid size={{ xs: 12 }}>
                {/* Rule Editor Mode Toggle */}
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                    Rule Expression *
                  </Typography>
                  <ToggleButtonGroup
                    value={ruleEditorMode}
                    exclusive
                    onChange={(_, newMode) => {
                      if (newMode !== null) {
                        setRuleEditorMode(newMode);
                      }
                    }}
                    size="small"
                  >
                    <ToggleButton value="text">
                      <CodeRoundedIcon fontSize="small" sx={{ mr: 1 }} />
                      Text Editor
                    </ToggleButton>
                    <ToggleButton value="visual">
                      <BuildRoundedIcon fontSize="small" sx={{ mr: 1 }} />
                      Visual Builder
                    </ToggleButton>
                  </ToggleButtonGroup>
                </Box>

                {ruleEditorMode === 'text' ? (
                  <>
                    <TextField
                      fullWidth multiline rows={6} required
                      label="Rule Expression"
                      value={form.ruleExpression}
                      onChange={set('ruleExpression')}
                      error={validationResult?.valid === false}
                      helperText={
                        validating ? 'Validating...' :
                        validationResult?.valid === false ? `❌ ${validationResult.error}` :
                        validationResult?.valid === true ? '✅ Expression is valid' :
                        'Enter a WHEN ... THEN ... rule expression'
                      }
                      inputProps={{
                        style: { fontFamily: 'monospace', fontSize: '0.85rem' },
                        ref: expressionRef,
                      }}
                      sx={{
                        '& .MuiInputBase-root': {
                          backgroundColor: validationResult?.valid === false ? 'error.50' :
                                           validationResult?.valid === true ? 'success.50' : undefined,
                        },
                      }}
                    />

                    {/* Syntax Guide */}
                    <Box sx={{ mt: 1, p: 2, bgcolor: 'background.paper', border: '1px solid',
                               borderColor: 'divider', borderRadius: 1 }}>
                      <Typography variant="subtitle2" sx={{ mb: 1, color: 'text.secondary' }}>
                        📖 Rule Language Syntax
                      </Typography>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace', mb: 1 }}>
                        <strong>Basic:</strong> WHEN [condition] THEN [reward]
                      </Typography>
                      <Typography variant="body2" component="div" sx={{ mb: 1 }}>
                        <strong>Conditions:</strong> source.field &gt; 50, source.field = "value", source.active = true
                      </Typography>
                      <Typography variant="body2" component="div" sx={{ mb: 1 }}>
                        <strong>Operators:</strong> &gt;, &lt;, &gt;=, &lt;=, =, !=, CONTAINS, STARTS_WITH, ENDS_WITH
                      </Typography>
                      <Typography variant="body2" component="div" sx={{ mb: 1 }}>
                        <strong>Logic:</strong> AND, OR, parentheses for grouping
                      </Typography>
                      <Typography variant="body2" component="div">
                        <strong>Rewards:</strong> Point(30) or Voucher(SUMMER25)
                      </Typography>

                      <Typography variant="caption" sx={{ mt: 1, display: 'block', fontStyle: 'italic' }}>
                        💡 Click field names below to insert them into your expression
                      </Typography>
                    </Box>
                  </>
                ) : (
                  <RuleBuilder
                    metadata={metadata}
                    initialExpression={form.ruleExpression}
                    onExpressionChange={(expression) => setForm(prev => ({ ...prev, ruleExpression: expression }))}
                  />
                )}

                {/* Validation Status */}
                {ruleEditorMode === 'visual' && (
                  <Box sx={{ mt: 1 }}>
                    {validating ? (
                      <Alert severity="info" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <CircularProgress size={16} />
                        Validating expression...
                      </Alert>
                    ) : validationResult?.valid === false ? (
                      <Alert severity="error">
                        ❌ {validationResult.error}
                      </Alert>
                    ) : validationResult?.valid === true ? (
                      <Alert severity="success">
                        ✅ Expression is valid
                      </Alert>
                    ) : null}
                  </Box>
                )}
              </Grid>

              {/* Row 4 — field browser (only in text mode) */}
              {ruleEditorMode === 'text' && (
                <Grid size={{ xs: 12 }}>
                  <FieldBrowser metadata={metadata} onInsert={handleInsertField} />
                </Grid>
              )}

              {/* Row 5 — frequency / active from / active to */}
              <Grid size={{ xs: 12, sm: 4 }}>
                <TextField
                  fullWidth label="Frequency (cron)"
                  value={form.frequency ?? ''}
                  onChange={set('frequency')}
                  helperText='e.g. "0 */5 * * * *"'
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <DatePicker
                  label="Active From"
                  value={form.activeFrom ? dayjs(form.activeFrom) : null}
                  onChange={(v: Dayjs | null) =>
                    setForm((p) => ({ ...p, activeFrom: v ? v.format('YYYY-MM-DD') : '' }))
                  }
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 4 }}>
                <DatePicker
                  label="Active To"
                  value={form.activeTo ? dayjs(form.activeTo) : null}
                  onChange={(v: Dayjs | null) =>
                    setForm((p) => ({ ...p, activeTo: v ? v.format('YYYY-MM-DD') : '' }))
                  }
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
            </Grid>
          )}

          {tab === 1 && isEdit && (
            <>
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
                <Button variant="outlined" startIcon={<AddRoundedIcon />} onClick={() => openActionDialog()}>
                  Add Action
                </Button>
              </Box>
              <DataGrid
                rows={actions}
                columns={actionColumns}
                autoHeight
                disableRowSelectionOnClick
                pageSizeOptions={[10]}
                sx={{ border: 0 }}
              />
            </>
          )}
        </CardContent>

        <Divider />
        <Box sx={{ p: 2, display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
          <Button onClick={() => navigate('/rules')}>Cancel</Button>
          <Button variant="contained" startIcon={<SaveRoundedIcon />} onClick={handleSave} disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </Button>
        </Box>
      </Card>

      {/* ── Reward Action Dialog ─────────────────────────────────────────── */}
      <Dialog open={actionDialog} onClose={() => setActionDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editActionId ? 'Edit Reward Action' : 'Add Reward Action'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth select label="Reward Type"
                value={actionForm.rewardType}
                onChange={(e) => setActionForm((p) => ({ ...p, rewardType: e.target.value as RewardType }))}
              >
                {(['POINT', 'VOUCHER'] as RewardType[]).map((t) => (
                  <MenuItem key={t} value={t}>{t}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth label="Amount / Value"
                value={actionForm.rewardAmount}
                onChange={(e) => setActionForm((p) => ({ ...p, rewardAmount: e.target.value }))}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth multiline rows={2} label="Description"
                value={actionForm.description ?? ''}
                onChange={(e) => setActionForm((p) => ({ ...p, description: e.target.value }))}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSaveAction}>Save</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={deleteActionId != null}
        title="Delete Action"
        message="Remove this reward action from the rule?"
        onConfirm={handleDeleteAction}
        onCancel={() => setDeleteActionId(null)}
      />
    </>
  );
}

