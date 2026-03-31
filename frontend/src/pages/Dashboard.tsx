import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, Typography, Box, CircularProgress, Chip } from '@mui/material';
import Grid from '@mui/material/Grid2';
import StorageRoundedIcon    from '@mui/icons-material/StorageRounded';
import GavelRoundedIcon      from '@mui/icons-material/GavelRounded';
import EmojiEventsRoundedIcon from '@mui/icons-material/EmojiEventsRounded';
import PageHeader from '../components/PageHeader';
import { getDataSources } from '../api/dataSources';
import { getRules }        from '../api/rules';
import type { DataSource, Rule } from '../types';

interface StatProps {
  label: string; value: number; color: string;
  icon: React.ReactNode; onClick?: () => void;
}

function StatCard({ label, value, color, icon, onClick }: StatProps) {
  return (
    <Card
      sx={{ cursor: onClick ? 'pointer' : 'default', transition: 'box-shadow .2s',
            '&:hover': onClick ? { boxShadow: 4 } : {} }}
      onClick={onClick}
    >
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>{label}</Typography>
            <Typography variant="h3" fontWeight={700}>{value}</Typography>
          </Box>
          <Box sx={{ bgcolor: `${color}18`, borderRadius: 3, p: 1.5, color }}>{icon}</Box>
        </Box>
      </CardContent>
    </Card>
  );
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [dataSources, setDataSources] = useState<DataSource[]>([]);
  const [rules, setRules]             = useState<Rule[]>([]);
  const [loading, setLoading]         = useState(true);

  useEffect(() => {
    Promise.all([getDataSources(), getRules()])
      .then(([ds, r]) => { setDataSources(ds.data); setRules(r.data); })
      .finally(() => setLoading(false));
  }, []);

  const activeRules  = rules.filter((r) => r.status === 'ACTIVE').length;
  const totalActions = rules.reduce((acc, r) => acc + (r.actions?.length ?? 0), 0);

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  return (
    <>
      <PageHeader title="Dashboard" />

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard label="Data Sources" value={dataSources.length} color="#1565c0"
            icon={<StorageRoundedIcon />} onClick={() => navigate('/data-sources')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard label="Active Rules" value={activeRules} color="#2e7d32"
            icon={<GavelRoundedIcon />} onClick={() => navigate('/rules')} />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <StatCard label="Reward Actions" value={totalActions} color="#f57c00"
            icon={<EmojiEventsRoundedIcon />} />
        </Grid>
      </Grid>

      {/* Recent Rules */}
      <Typography variant="h6" sx={{ mb: 2 }}>Recent Rules</Typography>
      <Card>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          {rules.slice(0, 5).map((rule, i) => (
            <Box
              key={rule.id}
              onClick={() => navigate(`/rules/${rule.id}`)}
              sx={{
                display: 'flex', alignItems: 'center', gap: 2, px: 3, py: 2,
                borderBottom: i < 4 ? '1px solid' : 'none', borderColor: 'divider',
                cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' },
              }}
            >
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Typography variant="subtitle2" noWrap>{rule.name}</Typography>
                <Typography variant="caption" color="text.secondary" noWrap>
                  {rule.ruleExpression}
                </Typography>
              </Box>
              <Chip
                label={rule.status}
                size="small"
                color={rule.status === 'ACTIVE' ? 'success' : rule.status === 'DRAFT' ? 'default' : 'warning'}
              />
            </Box>
          ))}
          {rules.length === 0 && (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="text.secondary">No rules configured yet.</Typography>
            </Box>
          )}
        </CardContent>
      </Card>
    </>
  );
}

