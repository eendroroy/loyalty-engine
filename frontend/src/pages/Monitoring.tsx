import { useEffect, useState } from 'react';
import {
  Box, Chip, Tab, Tabs, Alert, CircularProgress, Typography,
  Tooltip, LinearProgress,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import FiberManualRecordRoundedIcon from '@mui/icons-material/FiberManualRecordRounded';
import PageHeader from '../components/PageHeader';
import { getFileWatchers, getWebhooks, getLogs } from '../api/monitor';
import type { FileWatcherMonitor, WebhookMonitor, FileProcessingLog, FileProcessingStatus } from '../types';

// ── Helpers ───────────────────────────────────────────────────────────────────

const STATUS_COLOR: Record<FileProcessingStatus, 'success' | 'error' | 'warning'> = {
  COMPLETED: 'success', FAILED: 'error', IN_PROGRESS: 'warning',
};

function StatusChip({ status }: { status: FileProcessingStatus }) {
  return <Chip label={status} size="small" color={STATUS_COLOR[status]} variant="outlined" />;
}

function fmt(iso?: string) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString();
}

function fmtBytes(b?: number) {
  if (b == null) return '—';
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  return `${(b / 1024 / 1024).toFixed(1)} MB`;
}

// ── Columns ───────────────────────────────────────────────────────────────────

const watcherColumns: GridColDef<FileWatcherMonitor>[] = [
  {
    field: 'watching', headerName: '', width: 36, sortable: false,
    renderCell: ({ value }) => (
      <Tooltip title={value ? 'Watching' : 'Not watching'}>
        <FiberManualRecordRoundedIcon fontSize="small" sx={{ color: value ? 'success.main' : 'text.disabled' }} />
      </Tooltip>
    ),
  },
  { field: 'sourceName', headerName: 'Source',    flex: 1,   minWidth: 130 },
  {
    field: 'filePath', headerName: 'File Path', flex: 1.8, minWidth: 200,
    renderCell: ({ value }) =>
      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{value}</Typography>,
  },
  {
    field: 'lastLog', headerName: 'Last Result', width: 140, sortable: false,
    renderCell: ({ value }) => value ? <StatusChip status={value.status} /> : <Typography variant="caption" color="text.disabled">—</Typography>,
  },
  {
    field: 'lastLog_rows', headerName: 'Rows', width: 90, sortable: false,
    valueGetter: (_value: unknown, row: FileWatcherMonitor) => row.lastLog?.rowsIngested ?? '—',
  },
  {
    field: 'lastLog_at', headerName: 'Last Processed', width: 175, sortable: false,
    valueGetter: (_value: unknown, row: FileWatcherMonitor) => row.lastLog?.completedAt
      ? fmt(row.lastLog.completedAt) : '—',
  },
  {
    field: 'totalCompleted', headerName: '✓ Runs', width: 80,
    renderCell: ({ value }) => <Chip label={value} size="small" color="success" variant="outlined" />,
  },
  {
    field: 'totalFailed', headerName: '✗ Fails', width: 80,
    renderCell: ({ value }) =>
      value > 0
        ? <Chip label={value} size="small" color="error" variant="outlined" />
        : <Typography variant="caption" color="text.disabled">0</Typography>,
  },
];

const webhookColumns: GridColDef<WebhookMonitor>[] = [
  { field: 'sourceName', headerName: 'Source',      flex: 1,   minWidth: 130 },
  {
    field: 'endpoint', headerName: 'Webhook Path', flex: 1.5, minWidth: 180,
    renderCell: ({ value }) =>
      <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'secondary.main' }}>{value}</Typography>,
  },
  { field: 'description', headerName: 'Description', flex: 1.5, minWidth: 160 },
  {
    field: 'createdAt', headerName: 'Created', width: 175,
    valueGetter: (value: string) => fmt(value),
  },
];

const logColumns: GridColDef<FileProcessingLog>[] = [
  {
    field: 'status', headerName: 'Status', width: 130,
    renderCell: ({ value }) => <StatusChip status={value} />,
  },
  { field: 'sourceName', headerName: 'Source',    flex: 0.8, minWidth: 120 },
  {
    field: 'fileName', headerName: 'File', flex: 1.2, minWidth: 150,
    renderCell: ({ value }) =>
      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{value}</Typography>,
  },
  {
    field: 'fileSizeBytes', headerName: 'Size', width: 90,
    valueGetter: (value: number) => fmtBytes(value),
  },
  { field: 'rowsIngested', headerName: 'Ingested', width: 90,  type: 'number' },
  { field: 'rowsSkipped',  headerName: 'Skipped',  width: 85,  type: 'number' },
  { field: 'errors',       headerName: 'Errors',   width: 75,  type: 'number' },
  { field: 'instanceId',   headerName: 'Instance', width: 150 },
  {
    field: 'startedAt', headerName: 'Started', width: 175,
    valueGetter: (value: string) => fmt(value),
  },
  {
    field: 'completedAt', headerName: 'Completed', width: 175,
    valueGetter: (value: string) => fmt(value),
  },
];

// ── Page ──────────────────────────────────────────────────────────────────────

export default function Monitoring() {
  const [tab, setTab] = useState(0);

  const [watchers, setWatchers]   = useState<FileWatcherMonitor[]>([]);
  const [webhooks, setWebhooks]   = useState<WebhookMonitor[]>([]);
  const [logs, setLogs]           = useState<FileProcessingLog[]>([]);
  const [logTotal, setLogTotal]   = useState(0);
  const [logPage, setLogPage]     = useState(0);
  const [logSize]                 = useState(50);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState('');

  const loadWatchers = () => {
    setLoading(true);
    getFileWatchers()
      .then((r) => setWatchers(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  const loadWebhooks = () => {
    setLoading(true);
    getWebhooks()
      .then((r) => setWebhooks(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  const loadLogs = (page = 0) => {
    setLoading(true);
    getLogs(page, logSize)
      .then((r) => { setLogs(r.data.content); setLogTotal(r.data.totalElements); setLogPage(page); })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadWatchers(); loadWebhooks(); loadLogs(0); }, []);

  const watching   = watchers.filter((w) => w.watching).length;
  const totalFails = watchers.reduce((s, w) => s + w.totalFailed, 0);

  return (
    <>
      <PageHeader title="Monitor" crumbs={[{ label: 'Monitor' }]} />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* ── Summary chips ───────────────────────────────────────────── */}
      <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
        <Chip label={`${watching} / ${watchers.length} watching`} color={watching > 0 ? 'success' : 'default'} />
        <Chip label={`${webhooks.length} webhook${webhooks.length !== 1 ? 's' : ''}`} color="secondary" variant="outlined" />
        {totalFails > 0 && <Chip label={`${totalFails} failed runs`} color="error" variant="outlined" />}
      </Box>

      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label={`File Watchers (${watchers.length})`} />
          <Tab label={`Webhooks (${webhooks.length})`} />
          <Tab label={`Processing Log (${logTotal})`} />
        </Tabs>

        {loading && <LinearProgress />}

        {tab === 0 && (
          <DataGrid
            rows={watchers}
            getRowId={(r) => r.fileId}
            columns={watcherColumns}
            autoHeight
            disableRowSelectionOnClick
            pageSizeOptions={[25, 50]}
            sx={{ border: 0 }}
            getRowClassName={(params) =>
              params.row.lastLog?.status === 'FAILED' ? 'row--error' : ''}
          />
        )}

        {tab === 1 && (
          <DataGrid
            rows={webhooks}
            getRowId={(r) => r.webhookId}
            columns={webhookColumns}
            autoHeight
            disableRowSelectionOnClick
            pageSizeOptions={[25, 50]}
            sx={{ border: 0 }}
          />
        )}

        {tab === 2 && (
          <DataGrid
            rows={logs}
            getRowId={(r) => r.id}
            columns={logColumns}
            rowCount={logTotal}
            paginationMode="server"
            paginationModel={{ page: logPage, pageSize: logSize }}
            onPaginationModelChange={({ page }) => loadLogs(page)}
            pageSizeOptions={[50]}
            disableRowSelectionOnClick
            autoHeight
            sx={{ border: 0 }}
          />
        )}
      </Box>
    </>
  );
}

