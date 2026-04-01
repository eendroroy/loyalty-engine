import { useEffect, useState } from 'react';
import {
  Box, Chip, IconButton, Tooltip, Alert, Typography,
  Paper, Divider, Stack, useTheme, alpha,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import DeleteForeverRoundedIcon  from '@mui/icons-material/DeleteForeverRounded';
import TableRowsRoundedIcon      from '@mui/icons-material/TableRowsRounded';
import TableChartRoundedIcon     from '@mui/icons-material/TableChartRounded';
import StorageRoundedIcon        from '@mui/icons-material/StorageRounded';
import PageHeader    from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import {
  getArchivedDataSources,
  purgeArchivedSource,
  purgeDestinationData,
  dropDestinationTable,
} from '../api/dataSources';
import type { DataSource } from '../types';

type ActionKind = 'purge-data' | 'drop-table' | 'delete-source';

export default function ArchivedSources() {
  const theme  = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const accent = theme.palette.primary.main;

  const [rows, setRows]       = useState<DataSource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');
  const [busy, setBusy]       = useState(false);
  // Confirm dialog state
  const [confirmOpen, setConfirmOpen]   = useState(false);
  const [confirmTitle, setConfirmTitle] = useState('');
  const [confirmMsg, setConfirmMsg]     = useState('');
  const [pendingId, setPendingId]       = useState<number | null>(null);
  const [pendingKind, setPendingKind]   = useState<ActionKind | null>(null);
  const load = () => {
    setLoading(true);
    getArchivedDataSources()
      .then((r) => setRows(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);
  const ask = (id: number, kind: ActionKind, title: string, msg: string) => {
    setPendingId(id);
    setPendingKind(kind);
    setConfirmTitle(title);
    setConfirmMsg(msg);
    setConfirmOpen(true);
  };
  const handleConfirm = async () => {
    if (pendingId == null || pendingKind == null) return;
    setBusy(true);
    setError('');
    try {
      if (pendingKind === 'purge-data')    await purgeDestinationData(pendingId);
      if (pendingKind === 'drop-table')    await dropDestinationTable(pendingId);
      if (pendingKind === 'delete-source') await purgeArchivedSource(pendingId);
      setConfirmOpen(false);
      load();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Operation failed');
      setConfirmOpen(false);
    } finally {
      setBusy(false);
    }
  };
  const columns: GridColDef<DataSource>[] = [
    { field: 'name',        headerName: 'Name',        flex: 1,   minWidth: 160 },
    { field: 'description', headerName: 'Description', flex: 1.5, minWidth: 160 },
    {
      field: 'destinationTable', headerName: 'Destination Table', flex: 1, minWidth: 160,
      renderCell: ({ row }) => row.destinationTable
        ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <StorageRoundedIcon sx={{ fontSize: 14, color: 'warning.main' }} />
            <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'warning.dark' }}>
              {row.destinationTable}
            </Typography>
          </Box>
        )
        : <Typography variant="caption" color="text.disabled">—</Typography>,
    },
    {
      field: 'files', headerName: 'Files', width: 80, sortable: false,
      renderCell: ({ row }) => {
        const count = row.files?.length ?? 0;
        return count > 0
          ? <Chip label={count} size="small" color="default" variant="outlined" />
          : <Typography variant="caption" color="text.disabled">—</Typography>;
      },
    },
    {
      field: 'actions', headerName: '', width: 150, sortable: false,
      renderCell: ({ row }) => (
        <Stack direction="row" spacing={0.5}>
          <Tooltip title="Purge imported data (clears all rows, keeps table)">
            <span>
              <IconButton
                size="small"
                color="warning"
                disabled={!row.destinationTable}
                onClick={() => ask(
                  row.id!,
                  'purge-data',
                  'Purge Imported Data',
                  `Delete all ingested rows from table "${row.destinationTable}"? The table schema is kept. This is irreversible.`,
                )}
              >
                <TableRowsRoundedIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Drop destination table entirely (irreversible)">
            <span>
              <IconButton
                size="small"
                color="error"
                disabled={!row.destinationTable}
                onClick={() => ask(
                  row.id!,
                  'drop-table',
                  'Drop Destination Table',
                  `Drop table "${row.destinationTable}"? All data is permanently lost and the table cannot be recovered. This is irreversible.`,
                )}
              >
                <TableChartRoundedIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>
          <Tooltip title="Permanently delete this source">
            <IconButton
              size="small"
              color="error"
              onClick={() => ask(
                row.id!,
                'delete-source',
                'Delete Data Source',
                `Permanently delete "${row.name}"? This cannot be undone.`,
              )}
            >
              <DeleteForeverRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ),
    },
  ];
  return (
    <>
      <PageHeader
        title="Archived Sources"
        crumbs={[{ label: 'Archived Sources' }]}
      />

      <Paper
        variant="outlined"
        sx={{
          mb: 2,
          p: 2.5,
          bgcolor: isDark ? alpha(accent, 0.12) : alpha(accent, 0.06),
          border: `1px solid ${alpha(accent, isDark ? 0.25 : 0.18)}`,
          borderRadius: 3,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Accent border at left */}
        <Box
          sx={{
            position: 'absolute',
            left: 0, top: 0, bottom: 0,
            width: 4,
            bgcolor: accent,
          }}
        />

        <Box sx={{ pl: 1.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
            <Box
              sx={{
                width: 24, height: 24,
                borderRadius: '6px',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                bgcolor: alpha(accent, isDark ? 0.25 : 0.15),
                color: accent,
                fontSize: '0.9rem',
                fontWeight: 700,
              }}
            >
              ⚠
            </Box>
            <Typography variant="body2" sx={{ fontWeight: 600, color: isDark ? accent : 'error.dark' }}>
              These data sources are archived. File watchers are disabled. Actions below are irreversible.
            </Typography>
          </Box>

          <Divider sx={{ my: 1.5, borderColor: alpha(accent, isDark ? 0.15 : 0.12) }} />

          <Stack direction={{ xs: 'column', md: 'row' }} spacing={{ xs: 1, md: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              <TableRowsRoundedIcon sx={{ fontSize: 16, color: accent }} />
              <Typography variant="caption" color="text.primary" sx={{ fontWeight: 500 }}>
                Purge data — clears all rows, table stays
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              <TableChartRoundedIcon sx={{ fontSize: 16, color: accent }} />
              <Typography variant="caption" color="text.primary" sx={{ fontWeight: 500 }}>
                Drop table — removes the entire table
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              <DeleteForeverRoundedIcon sx={{ fontSize: 16, color: accent }} />
              <Typography variant="caption" color="text.primary" sx={{ fontWeight: 500 }}>
                Delete source — permanently removes the record
              </Typography>
            </Box>
          </Stack>
        </Box>
      </Paper>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={loading || busy}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick
          sx={{ border: 0 }}
        />
      </Box>
      <ConfirmDialog
        open={confirmOpen}
        title={confirmTitle}
        message={confirmMsg}
        onConfirm={handleConfirm}
        onCancel={() => setConfirmOpen(false)}
      />
    </>
  );
}
