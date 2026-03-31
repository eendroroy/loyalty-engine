import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Alert, Chip, IconButton, Tooltip, TextField,
  InputAdornment,
} from '@mui/material';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import StorageRoundedIcon       from '@mui/icons-material/StorageRounded';
import TableViewRoundedIcon     from '@mui/icons-material/TableViewRounded';
import SearchRoundedIcon        from '@mui/icons-material/SearchRounded';
import PageHeader from '../components/PageHeader';
import { getDataSourcesWithTables } from '../api/dataSources';
import type { DataSource } from '../types';

export default function ImportedData() {
  const navigate = useNavigate();
  const [rows, setRows]       = useState<DataSource[]>([]);
  const [filtered, setFiltered] = useState<DataSource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState('');
  const [search, setSearch]   = useState('');

  useEffect(() => {
    getDataSourcesWithTables()
      .then((r) => { setRows(r.data); setFiltered(r.data); })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const q = search.toLowerCase();
    setFiltered(
      q ? rows.filter((r) =>
        r.name.toLowerCase().includes(q) ||
        (r.destinationTable ?? '').toLowerCase().includes(q) ||
        (r.description ?? '').toLowerCase().includes(q)
      ) : rows
    );
  }, [search, rows]);

  const columns: GridColDef<DataSource>[] = [
    {
      field: 'name', headerName: 'Data Source', flex: 1, minWidth: 180,
      renderCell: ({ row }) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <StorageRoundedIcon sx={{ fontSize: 16, color: 'primary.main' }} />
          <Typography variant="body2" fontWeight={500}>{row.name}</Typography>
        </Box>
      ),
    },
    { field: 'description', headerName: 'Description', flex: 1.5, minWidth: 160 },
    {
      field: 'destinationTable', headerName: 'Destination Table', flex: 1, minWidth: 180,
      renderCell: ({ row }) => (
        <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'success.dark' }}>
          {row.destinationTable}
        </Typography>
      ),
    },
    {
      field: 'fields', headerName: 'Fields', width: 80, sortable: false,
      renderCell: ({ row }) => {
        const count = row.files?.reduce(
          (sum, f) => sum + (f.fields?.length ?? 0), 0
        ) ?? 0;
        return <Chip label={count} size="small" variant="outlined" />;
      },
    },
    {
      field: 'files', headerName: 'Files', width: 70, sortable: false,
      renderCell: ({ row }) => {
        const c = row.files?.length ?? 0;
        return c > 0
          ? <Chip label={c} size="small" color="primary" variant="outlined" />
          : <Typography variant="caption" color="text.disabled">—</Typography>;
      },
    },
    {
      field: '_actions', headerName: '', width: 80, sortable: false,
      renderCell: ({ row }) => (
        <Tooltip title="Browse imported data">
          <IconButton
            size="small"
            color="primary"
            onClick={() => navigate(`/imported-data/${row.id}`)}
          >
            <TableViewRoundedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Imported Data"
        crumbs={[{ label: 'Imported Data' }]}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box sx={{ mb: 2 }}>
        <TextField
          size="small"
          placeholder="Filter by name, table or description…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ width: 360 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchRoundedIcon fontSize="small" />
                </InputAdornment>
              ),
            },
          }}
        />
      </Box>

      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <DataGrid
          rows={filtered}
          columns={columns}
          loading={loading}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          disableRowSelectionOnClick
          sx={{ border: 0 }}
          slots={{
            noRowsOverlay: () => (
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center',
                         justifyContent: 'center', gap: 1, height: '100%', py: 6 }}>
                <StorageRoundedIcon sx={{ fontSize: 40, color: 'text.disabled' }} />
                <Typography variant="body2" color="text.secondary">
                  No data sources with destination tables found.
                </Typography>
                <Typography variant="caption" color="text.disabled">
                  Set a Destination Table on a Data Source to see ingested records here.
                </Typography>
              </Box>
            ),
          }}
        />
      </Box>
    </>
  );
}

