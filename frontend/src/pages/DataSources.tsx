import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Chip, IconButton, Tooltip, Alert, Typography } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon        from '@mui/icons-material/AddRounded';
import EditRoundedIcon       from '@mui/icons-material/EditRounded';
import VisibilityRoundedIcon from '@mui/icons-material/VisibilityRounded';
import ArchiveRoundedIcon    from '@mui/icons-material/ArchiveRounded';
import StorageRoundedIcon    from '@mui/icons-material/StorageRounded';
import PageHeader    from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import { getDataSources, archiveDataSource } from '../api/dataSources';
import type { DataSource } from '../types';


export default function DataSources() {
  const navigate = useNavigate();
  const [rows, setRows]               = useState<DataSource[]>([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState('');
  const [archiveId, setArchiveId]     = useState<number | null>(null);
  const [archiveName, setArchiveName] = useState('');

  const load = () => {
    setLoading(true);
    getDataSources()
      .then((r) => setRows(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleArchive = async () => {
    if (archiveId == null) return;
    await archiveDataSource(archiveId);
    setArchiveId(null);
    load();
  };

  const columns: GridColDef<DataSource>[] = [
    { field: 'name',        headerName: 'Name',        flex: 1,   minWidth: 160 },
    { field: 'description', headerName: 'Description', flex: 1.5, minWidth: 160 },
    {
      field: 'destinationTable', headerName: 'Destination Table', flex: 1, minWidth: 160,
      renderCell: ({ row }) => row.destinationTable
        ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <StorageRoundedIcon sx={{ fontSize: 14, color: 'success.main' }} />
            <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'success.dark' }}>
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
          ? <Chip label={count} size="small" color="primary" variant="outlined" />
          : <Typography variant="caption" color="text.disabled">—</Typography>;
      },
    },
    {
      field: 'webhook', headerName: 'Webhook', width: 110, sortable: false,
      renderCell: ({ row }) => {
        return row.webhook?.enabled
          ? <Chip label="Active" size="small" color="secondary" variant="outlined" />
          : <Typography variant="caption" color="text.disabled">—</Typography>;
      },
    },
    {
      field: 'fields', headerName: 'Schema Fields', width: 110, sortable: false,
      renderCell: ({ row }) => {
        const count = row.fields?.length ?? 0;
        return count > 0
          ? <Chip label={count} size="small" color="primary" variant="outlined" />
          : <Typography variant="caption" color="text.disabled">—</Typography>;
      },
    },
    {
      field: 'actions', headerName: '', width: 120, sortable: false,
      renderCell: ({ row }) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="View">
            <IconButton size="small" onClick={() => navigate(`/data-sources/${row.id}`)}>
              <VisibilityRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => navigate(`/data-sources/${row.id}/edit`)}>
              <EditRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Archive">
            <IconButton
              size="small"
              color="warning"
              onClick={() => { setArchiveId(row.id!); setArchiveName(row.name); }}
            >
              <ArchiveRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Data Sources"
        crumbs={[{ label: 'Data Sources' }]}
        action={{ label: 'Add Data Source', icon: <AddRoundedIcon />, onClick: () => navigate('/data-sources/new') }}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick
          sx={{ border: 0 }}
        />
      </Box>

      <ConfirmDialog
        open={archiveId != null}
        title="Archive Data Source"
        message={`Archive "${archiveName}"? All file watchers will be disabled. The source can be found in Archived Sources.`}
        onConfirm={handleArchive}
        onCancel={() => setArchiveId(null)}
      />
    </>
  );
}

