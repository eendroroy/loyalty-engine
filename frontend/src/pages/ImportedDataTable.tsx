import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Typography, Alert, CircularProgress, TextField, InputAdornment,
  Chip, Tooltip, IconButton, Divider,
} from '@mui/material';
import {
  DataGrid,
  type GridColDef,
  type GridPaginationModel,
  type GridSortModel,
} from '@mui/x-data-grid';
import SearchRoundedIcon    from '@mui/icons-material/SearchRounded';
import ClearRoundedIcon     from '@mui/icons-material/ClearRounded';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import PageHeader           from '../components/PageHeader';
import { getDataSource, getTableData } from '../api/dataSources';
import type { DataSource, FieldDataType, TableColumnMeta, TableDataPage } from '../types';

const TYPE_COLOR: Record<FieldDataType, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
  STRING: 'default', INTEGER: 'primary', DECIMAL: 'secondary', DATE: 'warning', BOOLEAN: 'error',
};

const DEFAULT_PAGE_SIZE = 20;

export default function ImportedDataTable() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const dsId = Number(id);

  const [dataSource, setDataSource]   = useState<DataSource | null>(null);
  const [tableData, setTableData]     = useState<TableDataPage | null>(null);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState('');

  const [search, setSearch]           = useState('');
  const [debouncedSearch, setDebounced] = useState('');
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({
    page: 0, pageSize: DEFAULT_PAGE_SIZE,
  });
  const [sortModel, setSortModel] = useState<GridSortModel>([]);

  // Debounce search
  const timerRef = useRef<ReturnType<typeof setTimeout>>();
  useEffect(() => {
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      setDebounced(search);
      setPaginationModel((p) => ({ ...p, page: 0 })); // reset to page 0 on new search
    }, 400);
    return () => clearTimeout(timerRef.current);
  }, [search]);

  // Load DataSource metadata once
  useEffect(() => {
    getDataSource(dsId)
      .then((r) => setDataSource(r.data))
      .catch((e) => setError(e.message));
  }, [dsId]);

  // Load table data on pagination / search / sort change
  const loadData = useCallback(() => {
    setLoading(true);
    const sort = sortModel[0];
    getTableData(dsId, {
      search: debouncedSearch || undefined,
      page: paginationModel.page,
      size: paginationModel.pageSize,
      sortBy: sort?.field,
      sortDir: sort?.sort ?? 'asc',
    })
      .then((r) => setTableData(r.data))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [dsId, debouncedSearch, paginationModel, sortModel]);

  useEffect(() => { loadData(); }, [loadData]);

  // Build DataGrid columns from the column metadata returned by the API
  const gridColumns = useMemo<GridColDef[]>(() => {
    if (!tableData?.columns?.length) return [];

    const metaCols: GridColDef[] = tableData.columns.map((col: TableColumnMeta) => ({
      field: col.alias,
      headerName: col.fieldName,
      flex: 1,
      minWidth: 120,
      sortable: true,
      renderHeader: () => (
        <Box>
          <Typography variant="caption" fontWeight={600} display="block" lineHeight={1.2}>
            {col.fieldName}
          </Typography>
          <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary', fontSize: '0.65rem' }}>
            {col.alias}
          </Typography>
        </Box>
      ),
      renderCell: ({ value }) => {
        if (value === null || value === undefined) {
          return <Typography variant="caption" color="text.disabled">—</Typography>;
        }
        if (col.dataType === 'BOOLEAN') {
          return (
            <Chip
              label={value ? 'true' : 'false'}
              size="small"
              color={value ? 'success' : 'default'}
              variant="outlined"
            />
          );
        }
        return (
          <Typography
            variant="caption"
            sx={col.dataType === 'INTEGER' || col.dataType === 'DECIMAL'
              ? { fontFamily: 'monospace', color: 'secondary.main' }
              : col.dataType === 'DATE'
                ? { color: 'warning.main' }
                : undefined}
          >
            {String(value)}
          </Typography>
        );
      },
    }));

    // Prepend the row id column
    return [
      {
        field: 'id',
        headerName: 'ID',
        width: 70,
        sortable: true,
        renderCell: ({ value }) => (
          <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.disabled' }}>
            {value}
          </Typography>
        ),
      },
      ...metaCols,
      {
        field: 'created_at',
        headerName: 'Ingested At',
        width: 170,
        sortable: true,
        renderCell: ({ value }) => (
          <Typography variant="caption" color="text.secondary">
            {value ? new Date(value as string).toLocaleString() : '—'}
          </Typography>
        ),
      },
    ];
  }, [tableData?.columns]);

  // Map row objects — DataGrid requires an 'id' field
  const gridRows = useMemo(
    () => (tableData?.rows ?? []).map((row, idx) => ({
      ...row,
      id: (row['id'] as number) ?? idx,
    })),
    [tableData?.rows]
  );

  if (!dataSource && loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  }

  const dsName = dataSource?.name ?? `DataSource #${dsId}`;
  const tableName = dataSource?.destinationTable;

  return (
    <>
      <PageHeader
        title={dsName}
        crumbs={[
          { label: 'Imported Data', to: '/imported-data' },
          { label: dsName },
        ]}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* ── Header bar ───────────────────────────────────────────────── */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2, flexWrap: 'wrap' }}>
        <Tooltip title="Back to Imported Data">
          <IconButton size="small" onClick={() => navigate('/imported-data')}>
            <ArrowBackRoundedIcon fontSize="small" />
          </IconButton>
        </Tooltip>

        {tableName && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
            <Typography variant="caption" color="text.secondary">Table:</Typography>
            <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'success.dark', fontWeight: 600 }}>
              {tableName}
            </Typography>
          </Box>
        )}

        {/* Column type legend */}
        {tableData?.columns?.length ? (
          <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', ml: 'auto' }}>
            {tableData.columns.map((col) => (
              <Tooltip key={col.alias} title={col.description ?? col.alias}>
                <Chip
                  label={`${col.fieldName} · ${col.dataType}`}
                  size="small"
                  variant="outlined"
                  color={TYPE_COLOR[col.dataType]}
                />
              </Tooltip>
            ))}
          </Box>
        ) : null}
      </Box>

      <Divider sx={{ mb: 2 }} />

      {/* ── Search bar ───────────────────────────────────────────────── */}
      <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
        <TextField
          size="small"
          placeholder="Search across all text columns…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ width: 380 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <SearchRoundedIcon fontSize="small" />
                </InputAdornment>
              ),
              endAdornment: search ? (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => setSearch('')}>
                    <ClearRoundedIcon fontSize="small" />
                  </IconButton>
                </InputAdornment>
              ) : null,
            },
          }}
        />
        {tableData && (
          <Typography variant="caption" color="text.secondary">
            {tableData.totalElements.toLocaleString()} record{tableData.totalElements !== 1 ? 's' : ''}
            {debouncedSearch ? ' matching' : ' total'}
          </Typography>
        )}
      </Box>

      {/* ── Data Grid ────────────────────────────────────────────────── */}
      <Box sx={{ bgcolor: 'background.paper', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
        <DataGrid
          rows={gridRows}
          columns={gridColumns}
          loading={loading}
          // Server-side pagination
          paginationMode="server"
          rowCount={tableData?.totalElements ?? 0}
          paginationModel={paginationModel}
          onPaginationModelChange={setPaginationModel}
          pageSizeOptions={[10, 20, 50, 100]}
          // Server-side sorting
          sortingMode="server"
          sortModel={sortModel}
          onSortModelChange={setSortModel}
          autoHeight
          disableRowSelectionOnClick
          sx={{ border: 0 }}
          columnHeaderHeight={52}
          slots={{
            noRowsOverlay: () => (
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center',
                         justifyContent: 'center', gap: 1, height: '100%', py: 6 }}>
                <Typography variant="body2" color="text.secondary">
                  {debouncedSearch
                    ? `No records matching "${debouncedSearch}".`
                    : 'No records ingested yet. Drop a file to start.'}
                </Typography>
              </Box>
            ),
          }}
        />
      </Box>
    </>
  );
}

