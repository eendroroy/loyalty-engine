import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card, CardContent, Typography, Box, CircularProgress, Chip,
  useTheme, alpha, Divider, LinearProgress,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip as ReTooltip, ResponsiveContainer,
} from 'recharts';
import StorageRoundedIcon       from '@mui/icons-material/StorageRounded';
import GavelRoundedIcon         from '@mui/icons-material/GavelRounded';
import EmojiEventsRoundedIcon   from '@mui/icons-material/EmojiEventsRounded';
import ConfirmationNumberRoundedIcon from '@mui/icons-material/ConfirmationNumberRounded';
import FiberManualRecordIcon    from '@mui/icons-material/FiberManualRecord';
import TrendingUpRoundedIcon    from '@mui/icons-material/TrendingUpRounded';
import { getDataSources }  from '../api/dataSources';
import { getRules }         from '../api/rules';
import { getVouchers }      from '../api/vouchers';
import { getFileWatchers }  from '../api/monitor';
import type { DataSource, Rule, Voucher, FileWatcherMonitor } from '../types';

// ── Simulated 14-day activity chart data ─────────────────────────────────────
function buildChartData() {
  const labels = ['Mar 19','Mar 20','Mar 21','Mar 22','Mar 23','Mar 24','Mar 25',
                  'Mar 26','Mar 27','Mar 28','Mar 29','Mar 30','Mar 31','Apr 1'];
  const base =   [12, 8, 24, 18, 30, 15, 42, 35, 28, 55, 48, 60, 72, 85];
  return labels.map((day, i) => ({
    day,
    triggers: base[i] + Math.floor(Math.random() * 8),
    points:   Math.floor(base[i] * 1.8),
  }));
}
const CHART_DATA = buildChartData();

// ── Stat Card ─────────────────────────────────────────────────────────────────
interface StatProps {
  label: string;
  value: number | string;
  sub?: string;
  color: string;
  icon: React.ReactNode;
  trend?: number;        // percentage change
  onClick?: () => void;
}

function StatCard({ label, value, sub, color, icon, trend, onClick }: StatProps) {
  const theme  = useTheme();
  const isDark = theme.palette.mode === 'dark';

  return (
    <Card
      onClick={onClick}
      sx={{
        cursor: onClick ? 'pointer' : 'default',
        position: 'relative',
        overflow: 'hidden',
        '&:hover': onClick
          ? {
              transform: 'translateY(-2px)',
              boxShadow: isDark
                ? `0 12px 40px rgba(0,0,0,.5), 0 0 0 1px ${alpha(color, 0.3)}`
                : `0 8px 28px rgba(15,23,42,.14), 0 0 0 1px ${alpha(color, 0.2)}`,
            }
          : {},
      }}
    >
      {/* accent bar at top */}
      <Box
        sx={{
          position: 'absolute',
          top: 0, left: 0, right: 0,
          height: 3,
          background: `linear-gradient(90deg, ${color} 0%, ${alpha(color, 0.3)} 100%)`,
        }}
      />
      <CardContent sx={{ pt: 2.5, pb: '16px !important' }}>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 2 }}>
          {/* icon bubble */}
          <Box
            sx={{
              width: 44,
              height: 44,
              borderRadius: '12px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: isDark
                ? `linear-gradient(135deg, ${alpha(color, 0.25)} 0%, ${alpha(color, 0.12)} 100%)`
                : `linear-gradient(135deg, ${alpha(color, 0.15)} 0%, ${alpha(color, 0.07)} 100%)`,
              color,
              border: `1px solid ${alpha(color, isDark ? 0.25 : 0.18)}`,
            }}
          >
            {icon}
          </Box>

          {/* trend badge */}
          {trend !== undefined && (
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 0.25,
                px: 1,
                py: 0.25,
                borderRadius: 2,
                bgcolor: trend >= 0 ? alpha('#10B981', isDark ? 0.15 : 0.09) : alpha('#EF4444', isDark ? 0.15 : 0.09),
                color: trend >= 0 ? '#10B981' : '#EF4444',
              }}
            >
              <TrendingUpRoundedIcon sx={{ fontSize: 12, transform: trend < 0 ? 'scaleY(-1)' : 'none' }} />
              <Typography sx={{ fontSize: '0.7rem', fontWeight: 700 }}>
                {trend >= 0 ? '+' : ''}{trend}%
              </Typography>
            </Box>
          )}
        </Box>

        <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1, mb: 0.5 }}>
          {value}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
          {label}
        </Typography>
        {sub && (
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.25, opacity: 0.7 }}>
            {sub}
          </Typography>
        )}
      </CardContent>
    </Card>
  );
}

// ── Custom Recharts Tooltip ───────────────────────────────────────────────────
function CustomTooltip({ active, payload, label }: any) {
  const theme  = useTheme();
  const isDark = theme.palette.mode === 'dark';
  if (!active || !payload?.length) return null;
  return (
    <Box
      sx={{
        bgcolor: isDark ? '#1A1B28' : '#fff',
        border: `1px solid ${isDark ? 'rgba(255,255,255,0.1)' : 'rgba(15,23,42,0.1)'}`,
        borderRadius: 2,
        px: 2,
        py: 1.5,
        boxShadow: isDark ? '0 8px 24px rgba(0,0,0,.6)' : '0 4px 16px rgba(15,23,42,.15)',
      }}
    >
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 0.5 }}>
        {label}
      </Typography>
      {payload.map((p: any) => (
        <Typography key={p.dataKey} variant="body2" sx={{ fontWeight: 600, color: p.color }}>
          {p.name}: {p.value}
        </Typography>
      ))}
    </Box>
  );
}

// ── Activity item ─────────────────────────────────────────────────────────────
interface ActivityItem { text: string; sub: string; color: string; time: string }

// ── Main Dashboard ────────────────────────────────────────────────────────────
export default function Dashboard() {
  const navigate   = useNavigate();
  const theme      = useTheme();
  const isDark     = theme.palette.mode === 'dark';
  const accent     = theme.palette.primary.main;

  const [dataSources, setDataSources]   = useState<DataSource[]>([]);
  const [rules, setRules]               = useState<Rule[]>([]);
  const [vouchers, setVouchers]         = useState<Voucher[]>([]);
  const [watchers, setWatchers]         = useState<FileWatcherMonitor[]>([]);
  const [loading, setLoading]           = useState(true);

  useEffect(() => {
    Promise.all([getDataSources(), getRules(), getVouchers(), getFileWatchers()])
      .then(([ds, r, v, w]) => {
        setDataSources(ds.data);
        setRules(r.data);
        setVouchers(v.data);
        setWatchers(w.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress color="primary" size={36} />
      </Box>
    );
  }

  const activeRules      = rules.filter((r) => r.status === 'ACTIVE').length;
  const draftRules       = rules.filter((r) => r.status === 'DRAFT').length;
  const activeVouchers   = vouchers.filter((v) => v.active && !v.archived).length;
  const watchingCount    = watchers.filter((w) => w.watching).length;
  const totalVouchersIss = vouchers.reduce((s, v) => s + (v.instanceCount ?? 0), 0);

  // Simulate recent activity from rules
  const recentActivity: ActivityItem[] = [
    ...rules.slice(0, 3).map(r => ({
      text:  r.name,
      sub:   r.status === 'ACTIVE' ? 'Rule is active and evaluating' : `Status: ${r.status}`,
      color: r.status === 'ACTIVE' ? '#10B981' : r.status === 'DRAFT' ? '#F59E0B' : '#94A3B8',
      time:  r.updatedAt ? new Date(r.updatedAt).toLocaleDateString() : 'Recently',
    })),
    ...watchers.slice(0, 2).map(w => ({
      text:  w.sourceName,
      sub:   w.watching ? `Watching · ${w.totalCompleted} files processed` : 'Watcher paused',
      color: w.watching ? '#3B82F6' : '#94A3B8',
      time:  'Live',
    })),
  ];

  return (
    <Box>
      {/* ── Page title row ──────────────────────────────────────────────── */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 700, letterSpacing: '-0.02em' }}>
            Overview
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
            Welcome back — here's what's happening in your loyalty platform
          </Typography>
        </Box>
        <Box
          sx={{
            px: 1.5,
            py: 0.5,
            borderRadius: 2,
            bgcolor: alpha(accent, isDark ? 0.15 : 0.08),
            border: `1px solid ${alpha(accent, 0.25)}`,
            color: accent,
          }}
        >
          <Typography sx={{ fontSize: '0.75rem', fontWeight: 600 }}>
            April 1, 2026
          </Typography>
        </Box>
      </Box>

      {/* ── Stat Cards ──────────────────────────────────────────────────── */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatCard
            label="Data Sources"
            value={dataSources.length}
            sub={`${dataSources.filter(d => !d.archived).length} active`}
            color="#3B82F6"
            icon={<StorageRoundedIcon fontSize="small" />}
            trend={12}
            onClick={() => navigate('/data-sources')}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatCard
            label="Active Rules"
            value={activeRules}
            sub={`${draftRules} drafts pending`}
            color={accent}
            icon={<GavelRoundedIcon fontSize="small" />}
            trend={activeRules > 0 ? 8 : 0}
            onClick={() => navigate('/rules')}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatCard
            label="Vouchers Issued"
            value={totalVouchersIss}
            sub={`${activeVouchers} templates active`}
            color="#10B981"
            icon={<ConfirmationNumberRoundedIcon fontSize="small" />}
            trend={totalVouchersIss > 0 ? 24 : 0}
            onClick={() => navigate('/vouchers')}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatCard
            label="Live Monitors"
            value={watchingCount}
            sub={`${watchers.length} total watchers`}
            color="#F59E0B"
            icon={<EmojiEventsRoundedIcon fontSize="small" />}
            onClick={() => navigate('/monitor')}
          />
        </Grid>
      </Grid>

      {/* ── Chart + Activity ────────────────────────────────────────────── */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        {/* Area Chart */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 0.5 }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>Rule Activity</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Rule triggers over the last 14 days
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  {[
                    { label: 'Triggers', color: accent },
                    { label: 'Points', color: '#3B82F6' },
                  ].map(({ label, color }) => (
                    <Box key={label} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: color }} />
                      <Typography variant="caption" color="text.secondary">{label}</Typography>
                    </Box>
                  ))}
                </Box>
              </Box>

              <Box sx={{ height: 260, mt: 2 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={CHART_DATA} margin={{ top: 4, right: 4, bottom: 0, left: -24 }}>
                    <defs>
                      <linearGradient id="gradRed" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%"  stopColor={accent}   stopOpacity={isDark ? 0.7 : 0.5} />
                        <stop offset="95%" stopColor={accent}   stopOpacity={0} />
                      </linearGradient>
                      <linearGradient id="gradBlue" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%"  stopColor="#3B82F6" stopOpacity={isDark ? 0.5 : 0.35} />
                        <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke={isDark ? 'rgba(255,255,255,0.05)' : 'rgba(15,23,42,0.06)'}
                      vertical={false}
                    />
                    <XAxis
                      dataKey="day"
                      tick={{ fontSize: 11, fill: isDark ? '#475569' : '#94A3B8' }}
                      axisLine={false}
                      tickLine={false}
                      interval={1}
                    />
                    <YAxis
                      tick={{ fontSize: 11, fill: isDark ? '#475569' : '#94A3B8' }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <ReTooltip content={<CustomTooltip />} />
                    <Area
                      type="monotone"
                      dataKey="triggers"
                      name="Triggers"
                      stroke={accent}
                      strokeWidth={2.5}
                      fill="url(#gradRed)"
                      dot={false}
                      activeDot={{ r: 5, fill: accent, strokeWidth: 0 }}
                    />
                    <Area
                      type="monotone"
                      dataKey="points"
                      name="Points"
                      stroke="#3B82F6"
                      strokeWidth={2}
                      fill="url(#gradBlue)"
                      dot={false}
                      activeDot={{ r: 4, fill: '#3B82F6', strokeWidth: 0 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Activity Feed */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>Activity Feed</Typography>
              <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
                Recent system events
              </Typography>

              {recentActivity.length === 0 && (
                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center' }}>
                    No activity yet.<br />Configure rules and data sources to get started.
                  </Typography>
                </Box>
              )}

              <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 0 }}>
                {recentActivity.map((item, i) => (
                  <Box key={i}>
                    <Box sx={{ display: 'flex', gap: 1.5, py: 1.5, alignItems: 'flex-start' }}>
                      {/* colored dot */}
                      <Box sx={{ pt: 0.4, flexShrink: 0 }}>
                        <FiberManualRecordIcon sx={{ fontSize: 10, color: item.color }} />
                      </Box>
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.3 }} noWrap>
                          {item.text}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" noWrap>
                          {item.sub}
                        </Typography>
                      </Box>
                      <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, opacity: 0.7 }}>
                        {item.time}
                      </Typography>
                    </Box>
                    {i < recentActivity.length - 1 && <Divider />}
                  </Box>
                ))}
              </Box>

              {/* Rule health bar */}
              {rules.length > 0 && (
                <>
                  <Divider sx={{ my: 2 }} />
                  <Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
                        Rule Health
                      </Typography>
                      <Typography variant="caption" sx={{ fontWeight: 700, color: accent }}>
                        {rules.length > 0 ? Math.round((activeRules / rules.length) * 100) : 0}% active
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={rules.length > 0 ? (activeRules / rules.length) * 100 : 0}
                      sx={{
                        height: 6,
                        borderRadius: 3,
                        bgcolor: isDark ? alpha(accent, 0.12) : alpha(accent, 0.08),
                        '& .MuiLinearProgress-bar': {
                          borderRadius: 3,
                          background: `linear-gradient(90deg, ${accent} 0%, ${alpha(accent, 0.6)} 100%)`,
                        },
                      }}
                    />
                  </Box>
                </>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* ── Recent Rules ────────────────────────────────────────────────── */}
      <Card>
        <CardContent sx={{ pb: '0 !important' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Recent Rules</Typography>
              <Typography variant="caption" color="text.secondary">
                {rules.length} total rules configured
              </Typography>
            </Box>
            <Chip
              label="View all"
              size="small"
              onClick={() => navigate('/rules')}
              sx={{
                cursor: 'pointer',
                bgcolor: alpha(accent, isDark ? 0.15 : 0.08),
                color: accent,
                fontWeight: 600,
                '&:hover': { bgcolor: alpha(accent, isDark ? 0.22 : 0.14) },
              }}
            />
          </Box>

          {rules.length === 0 ? (
            <Box sx={{ py: 5, textAlign: 'center' }}>
              <Typography color="text.secondary" variant="body2">
                No rules configured yet. Create your first rule to get started.
              </Typography>
            </Box>
          ) : (
            rules.slice(0, 6).map((rule, i) => (
              <Box key={rule.id}>
                <Box
                  onClick={() => navigate(`/rules/${rule.id}`)}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 2,
                    py: 1.75,
                    cursor: 'pointer',
                    borderRadius: 2,
                    px: 1,
                    mx: -1,
                    transition: 'background 0.15s',
                    '&:hover': { bgcolor: isDark ? alpha('#fff', 0.03) : alpha('#000', 0.02) },
                  }}
                >
                  {/* Priority badge */}
                  <Box
                    sx={{
                      width: 28, height: 28,
                      borderRadius: '8px',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      bgcolor: alpha(accent, isDark ? 0.15 : 0.08),
                      color: accent,
                      fontWeight: 700,
                      fontSize: '0.7rem',
                      flexShrink: 0,
                    }}
                  >
                    {rule.priority}
                  </Box>

                  {/* Rule info */}
                  <Box sx={{ flex: 1, minWidth: 0 }}>
                    <Typography variant="body2" noWrap sx={{ fontWeight: 600 }}>
                      {rule.name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" noWrap sx={{ fontFamily: 'monospace' }}>
                      {rule.ruleExpression || '—'}
                    </Typography>
                  </Box>

                  {/* Actions count */}
                  {rule.actions && rule.actions.length > 0 && (
                    <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0 }}>
                      {rule.actions.length} action{rule.actions.length !== 1 ? 's' : ''}
                    </Typography>
                  )}

                  {/* Status chip */}
                  <Chip
                    label={rule.status}
                    size="small"
                    color={
                      rule.status === 'ACTIVE' ? 'success' :
                      rule.status === 'DRAFT'  ? 'warning' : 'default'
                    }
                  />
                </Box>
                {i < Math.min(rules.length, 6) - 1 && <Divider />}
              </Box>
            ))
          )}
          <Box sx={{ pb: 1 }} />
        </CardContent>
      </Card>
    </Box>
  );
}

