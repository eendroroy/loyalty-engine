import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Chip, IconButton, Tooltip, Alert, Tab, Tabs, Typography,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon         from '@mui/icons-material/AddRounded';
import EditRoundedIcon        from '@mui/icons-material/EditRounded';
import VisibilityRoundedIcon  from '@mui/icons-material/VisibilityRounded';
import ArchiveRoundedIcon     from '@mui/icons-material/ArchiveRounded';
import DeleteForeverRoundedIcon from '@mui/icons-material/DeleteForeverRounded';
import PageHeader    from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import {
  getVouchers, getArchivedVouchers, archiveVoucher, purgeVoucher,
} from '../api/vouchers';
import type { Voucher } from '../types';

export default function Vouchers() {
  const navigate = useNavigate();
  const [rows, setRows]             = useState<Voucher[]>([]);
  const [archivedRows, setArchived] = useState<Voucher[]>([]);
  const [tab, setTab]               = useState(0);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [archiveId, setArchiveId]   = useState<number | null>(null);
  const [purgeId, setPurgeId]       = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    Promise.all([getVouchers(), getArchivedVouchers()])
      .then(([a, b]) => { setRows(a.data); setArchived(b.data); })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);

  const handleArchive = async () => {
    if (archiveId == null) return;
    await archiveVoucher(archiveId);
    setArchiveId(null);
    load();
  };

  const handlePurge = async () => {
    if (purgeId == null) return;
    await purgeVoucher(purgeId);
    setPurgeId(null);
    load();
  };

  const baseColumns: GridColDef<Voucher>[] = [
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 160 },
    { field: 'code', headerName: 'Code', width: 140,
      renderCell: ({ value }) => (
        <Box component="span" sx={{ fontFamily: 'monospace', fontWeight: 600, color: 'primary.main' }}>
          {value}
        </Box>
      ),
    },
    {
      field: 'voucherType', headerName: 'Type', width: 120,
      renderCell: ({ value }) => <Chip label={value} size="small" color="secondary" variant="outlined" />,
    },
    {
      field: 'active', headerName: 'Status', width: 110,
      renderCell: ({ value }) => (
        <Chip
          label={value ? 'Active' : 'Inactive'}
          size="small"
          color={value ? 'success' : 'default'}
        />
      ),
    },
    { field: 'count',         headerName: 'Max Codes', width: 110, type: 'number' },
    { field: 'instanceCount', headerName: 'Awarded',   width: 100, type: 'number' },
    {
      field: 'createdAt', headerName: 'Created', width: 170,
      valueFormatter: (value) => value ? new Date(value).toLocaleDateString() : '—',
    },
  ];

  const activeColumns: GridColDef<Voucher>[] = [
    ...baseColumns,
    {
      field: 'actions', headerName: '', width: 130, sortable: false,
      renderCell: ({ row }) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="View">
            <IconButton size="small" onClick={() => navigate(`/vouchers/${row.id}`)}>
              <VisibilityRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => navigate(`/vouchers/${row.id}/edit`)}>
              <EditRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Archive">
            <IconButton size="small" color="warning" onClick={() => setArchiveId(row.id!)}>
              <ArchiveRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
  ];

  const archivedColumns: GridColDef<Voucher>[] = [
    ...baseColumns,
    {
      field: 'actions', headerName: '', width: 80, sortable: false,
      renderCell: ({ row }) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="Purge (permanent delete)">
            <IconButton size="small" color="error" onClick={() => setPurgeId(row.id!)}>
              <DeleteForeverRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
  ];

  const emptyOverlay = (msg: string) => () => (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', py: 3 }}>
      <Typography variant="body2" color="text.secondary">{msg}</Typography>
    </Box>
  );

  return (
    <>
      <PageHeader
        title="Vouchers"
        crumbs={[{ label: 'Vouchers' }]}
        action={{ label: 'Add Voucher', icon: <AddRoundedIcon />, onClick: () => navigate('/vouchers/new') }}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <Tabs
          value={tab}
          onChange={(_, v) => setTab(v)}
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab label={`Active (${rows.length})`} />
          <Tab label={`Archived (${archivedRows.length})`} />
        </Tabs>

        {tab === 0 && (
          <DataGrid
            rows={rows} columns={activeColumns} loading={loading} autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            disableRowSelectionOnClick sx={{ border: 0 }}
            slots={{ noRowsOverlay: emptyOverlay('No vouchers yet.') }}
          />
        )}

        {tab === 1 && (
          <DataGrid
            rows={archivedRows} columns={archivedColumns} loading={loading} autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            disableRowSelectionOnClick sx={{ border: 0 }}
            slots={{ noRowsOverlay: emptyOverlay('No archived vouchers.') }}
          />
        )}
      </Box>

      <ConfirmDialog
        open={archiveId != null}
        title="Archive Voucher"
        message="This will deactivate the voucher and prevent new codes from being awarded. Existing instances are preserved."
        onConfirm={handleArchive}
        onCancel={() => setArchiveId(null)}
      />

      <ConfirmDialog
        open={purgeId != null}
        title="Purge Voucher"
        message="This will permanently delete the voucher and all its awarded instances. This action is irreversible."
        onConfirm={handlePurge}
        onCancel={() => setPurgeId(null)}
      />
    </>
  );
}

