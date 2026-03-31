import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Chip, IconButton, Tooltip, Alert } from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import AddRoundedIcon    from '@mui/icons-material/AddRounded';
import EditRoundedIcon   from '@mui/icons-material/EditRounded';
import DeleteRoundedIcon from '@mui/icons-material/DeleteRounded';
import PageHeader from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import { getRules, deleteRule } from '../api/rules';
import type { Rule, RuleStatus } from '../types';

const STATUS_COLOR: Record<RuleStatus, 'success' | 'default' | 'warning'> = {
  ACTIVE: 'success', DRAFT: 'default', INACTIVE: 'warning',
};

export default function Rules() {
  const navigate = useNavigate();
  const [rows, setRows]         = useState<Rule[]>([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    getRules()
      .then((r) => setRows(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };
  useEffect(load, []);

  const handleDelete = async () => {
    if (deleteId == null) return;
    await deleteRule(deleteId);
    setDeleteId(null);
    load();
  };

  const columns: GridColDef<Rule>[] = [
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 160 },
    {
      field: 'status', headerName: 'Status', width: 120,
      renderCell: ({ value }) => (
        <Chip label={value} size="small" color={STATUS_COLOR[value as RuleStatus] ?? 'default'} />
      ),
    },
    { field: 'priority', headerName: 'Priority', width: 100, type: 'number' },
    {
      field: 'ruleExpression', headerName: 'Expression', flex: 2, minWidth: 200,
      renderCell: ({ value }) => (
        <Box component="span" sx={{ fontFamily: 'monospace', fontSize: '0.78rem', color: 'text.secondary' }}>
          {value}
        </Box>
      ),
    },
    { field: 'frequency', headerName: 'Frequency', width: 150 },
    {
      field: 'lastRunAt', headerName: 'Last Run', width: 170,
      valueFormatter: (value) => value ? new Date(value).toLocaleString() : '—',
    },
    {
      field: 'actions', headerName: '', width: 100, sortable: false,
      renderCell: ({ row }) => (
        <Box>
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => navigate(`/rules/${row.id}`)}>
              <EditRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Delete">
            <IconButton size="small" color="error" onClick={() => setDeleteId(row.id!)}>
              <DeleteRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Rules"
        crumbs={[{ label: 'Rules' }]}
        action={{ label: 'Add Rule', icon: <AddRoundedIcon />, onClick: () => navigate('/rules/new') }}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <DataGrid
          rows={rows} columns={columns} loading={loading} autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick sx={{ border: 0 }}
        />
      </Box>

      <ConfirmDialog
        open={deleteId != null} title="Delete Rule"
        message="This will permanently delete the rule and all its reward actions."
        onConfirm={handleDelete} onCancel={() => setDeleteId(null)}
      />
    </>
  );
}

