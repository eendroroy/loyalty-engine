import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Chip,
  Typography, Divider, Alert, CircularProgress,
  IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogActions,
  Accordion, AccordionSummary, AccordionDetails, MenuItem, Tabs, Tab,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef, type GridRenderCellParams } from '@mui/x-data-grid';
import AddRoundedIcon          from '@mui/icons-material/AddRounded';
import EditRoundedIcon         from '@mui/icons-material/EditRounded';
import DeleteRoundedIcon       from '@mui/icons-material/DeleteRounded';
import SaveRoundedIcon         from '@mui/icons-material/SaveRounded';
import ExpandMoreRoundedIcon   from '@mui/icons-material/ExpandMoreRounded';
import FolderRoundedIcon       from '@mui/icons-material/FolderRounded';
import WebhookRoundedIcon      from '@mui/icons-material/WebhookRounded';
import TableChartRoundedIcon   from '@mui/icons-material/TableChartRounded';
import VisibilityRoundedIcon   from '@mui/icons-material/VisibilityRounded';

import PageHeader    from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import {
  getDataSource, createDataSource, updateDataSource,
  type FieldPayload, type FilePayload, type WebhookPayload,
  type SchemaFieldPayload,
} from '../api/dataSources';
import type {
  DataSourceField, DataSourceSchemaField, FieldDataType, WebhookProperty, WebhookContentType,
} from '../types';

// ...existing type definitions...

interface Props { readOnly?: boolean }

interface SourceForm  { name: string; description: string; destinationTable: string };
interface WebhookForm { enabled: boolean; description: string };  // enabled flag + description

const EMPTY_SOURCE: SourceForm              = { name: '', description: '', destinationTable: '' };
const EMPTY_SCHEMA_FIELD: SchemaFieldPayload = { name: '', description: '', dataType: 'STRING' };
const EMPTY_FIELD: FieldPayload             = {
  fieldName: '', fieldAlias: '', dataType: 'STRING', columnNumber: undefined, description: '', dateFormat: '',
};
const EMPTY_FILE: FilePayload = {
  filePath: '', description: '', fieldSeparator: '', quoteCharacter: '',
  lineSeparator: '', skipFirstNLines: 0, archiveDirectory: '', fields: [],
};
const EMPTY_WEBHOOK: WebhookForm = { enabled: false, description: '' };

const DATA_TYPES: FieldDataType[] = ['STRING', 'INTEGER', 'DECIMAL', 'DATE', 'BOOLEAN'];
const DATA_TYPE_COLOR: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
  STRING: 'default', INTEGER: 'primary', DECIMAL: 'secondary', DATE: 'warning', BOOLEAN: 'error',
};

// ── Component ────────────────────────────────────────────────────────────────

export default function DataSourceForm({ readOnly = false }: Props) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit   = !!id && !readOnly;

  const [source, setSource]             = useState<SourceForm>(EMPTY_SOURCE);
  const [schemaFields, setSchemaFields] = useState<SchemaFieldPayload[]>([]);
  const [files, setFiles]               = useState<FilePayload[]>([]);
  const [webhook, setWebhook]           = useState<WebhookForm>(EMPTY_WEBHOOK);  // webhook form state
  const [loading, setLoading] = useState(!!id);
  const [saving, setSaving]   = useState(false);
  const [error, setError]     = useState('');
  const [step, setStep]       = useState(0);

  // schema field dialog
  const [sfDialog, setSfDialog]         = useState(false);
  const [sfForm, setSfForm]             = useState<SchemaFieldPayload>(EMPTY_SCHEMA_FIELD);
  const [editSfIdx, setEditSfIdx]       = useState<number | null>(null);
  const [deleteSfIdx, setDeleteSfIdx]   = useState<number | null>(null);

  // file dialog
  const [fileDialog, setFileDialog]       = useState(false);
  const [fileForm, setFileForm]           = useState<FilePayload>(EMPTY_FILE);
  const [editFileIdx, setEditFileIdx]     = useState<number | null>(null);
  const [deleteFileIdx, setDeleteFileIdx] = useState<number | null>(null);

  // file-field dialog
  const [ffDialog, setFfDialog]         = useState(false);
  const [ffForm, setFfForm]             = useState<FieldPayload>(EMPTY_FIELD);
  const [editFfIdx, setEditFfIdx]       = useState<number | null>(null);
  const [deleteFfIdx, setDeleteFfIdx]   = useState<number | null>(null);


  // ── Load ──────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (!id) return;
    getDataSource(Number(id))
      .then((r) => {
        const { fields: dsFields, files: fs, webhook: wh, ...rest } = r.data;
        setSource({ name: rest.name, description: rest.description ?? '', destinationTable: rest.destinationTable ?? '' });
        setSchemaFields(
          (dsFields ?? []).map(({ id: _i, createdAt: _c, updatedAt: _u, ...sf }: DataSourceSchemaField) =>
            sf as SchemaFieldPayload)
        );
        setFiles(
          (fs ?? []).map(({ id: _fid, createdAt: _c, updatedAt: _u, fields: fileFields, ...fp }) => ({
            ...fp,
            fieldSeparator: fp.fieldSeparator ?? '',
            quoteCharacter: fp.quoteCharacter ?? '',
            lineSeparator:  fp.lineSeparator  ?? '',
            skipFirstNLines: fp.skipFirstNLines ?? 0,
            archiveDirectory: fp.archiveDirectory ?? '',
            fields: (fileFields ?? []).map(({ id: _fid2, ...field }: DataSourceField) => ({
              ...field,
              dateFormat: field.dateFormat ?? '',
            } as FieldPayload)),
          } as FilePayload))
        );
        setWebhook({ enabled: wh?.enabled ?? false, description: wh?.description ?? '' });
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  // ── Save ──────────────────────────────────────────────────────────────────
  const handleSave = async () => {
    setSaving(true); setError('');
    try {
      const payload = {
        ...source,
        destinationTable: source.destinationTable || undefined,
        fields: schemaFields.length ? schemaFields.map((f) => ({ name: f.name, dataType: f.dataType, description: f.description || undefined })) : undefined,
        files: files.map((f) => ({
          ...f,
          fieldSeparator:   f.fieldSeparator   || undefined,
          quoteCharacter:   f.quoteCharacter   || undefined,
          lineSeparator:    f.lineSeparator    || undefined,
          skipFirstNLines:  f.skipFirstNLines  || undefined,
          archiveDirectory: f.archiveDirectory || undefined,
          fields: f.fields?.length ? f.fields.map((field) => ({
            ...field,
            dateFormat: field.dateFormat || undefined,
          })) : undefined,
        })),
        webhook: { enabled: webhook.enabled, description: webhook.description || undefined },
      };
      if (isEdit) await updateDataSource(Number(id), payload);
      else        await createDataSource(payload);
      navigate('/data-sources');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Save failed');
    } finally { setSaving(false); }
  };

  const canGoNext   = step !== 0 || Boolean(source.name.trim());
  const totalFields = files.reduce((sum, f) => sum + (f.fields?.length ?? 0), 0);

  // helpers
  const openAddSf  = () => { setSfForm({ ...EMPTY_SCHEMA_FIELD }); setEditSfIdx(null); setSfDialog(true); };
  const openEditSf = (i: number) => { setSfForm({ ...schemaFields[i] }); setEditSfIdx(i); setSfDialog(true); };
  const commitSf   = () => {
    setSchemaFields((p) => editSfIdx !== null ? p.map((f, i) => i === editSfIdx ? sfForm : f) : [...p, sfForm]);
    setSfDialog(false);
  };

  const openAddFile  = () => { setFileForm({ ...EMPTY_FILE, fields: [] }); setEditFileIdx(null); setFileDialog(true); };
  const openEditFile = (i: number) => { setFileForm({ ...files[i], fields: (files[i].fields ?? []).map((f) => ({ ...f })) }); setEditFileIdx(i); setFileDialog(true); };
  const commitFile   = () => { setFiles((p) => editFileIdx !== null ? p.map((f, i) => i === editFileIdx ? fileForm : f) : [...p, fileForm]); setFileDialog(false); };

  const openAddFf  = () => { setFfForm(EMPTY_FIELD); setEditFfIdx(null); setFfDialog(true); };
  const openEditFf = (i: number) => { setFfForm({ ...(fileForm.fields ?? [])[i] }); setEditFfIdx(i); setFfDialog(true); };
  const commitFf   = () => {
    setFileForm((p) => ({ ...p, fields: editFfIdx !== null ? (p.fields ?? []).map((f, i) => i === editFfIdx ? ffForm : f) : [...(p.fields ?? []), ffForm] }));
    setFfDialog(false);
  };
  const removeFf = (i: number) => { setFileForm((p) => ({ ...p, fields: (p.fields ?? []).filter((_, j) => j !== i) })); setDeleteFfIdx(null); };


  // columns
  const sfCols: GridColDef[] = [
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 140,
      renderCell: ({ value }: GridRenderCellParams) => <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'primary.main' }}>{value}</Typography> },
    { field: 'dataType', headerName: 'Target Type', width: 110,
      renderCell: ({ value }: GridRenderCellParams) => <Chip label={value} size="small" color={DATA_TYPE_COLOR[value] ?? 'default'} variant="outlined" /> },
    { field: 'description', headerName: 'Description', flex: 2, minWidth: 100 },
    ...(!readOnly ? [{ field: '_a', headerName: '', width: 80, sortable: false,
      renderCell: ({ row }: GridRenderCellParams) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title="Edit"><IconButton size="small" onClick={() => openEditSf(row._idx)}><EditRoundedIcon fontSize="small" /></IconButton></Tooltip>
          <Tooltip title="Remove"><IconButton size="small" color="error" onClick={() => setDeleteSfIdx(row._idx)}><DeleteRoundedIcon fontSize="small" /></IconButton></Tooltip>
        </Box>
      ) }] : []),
  ];

  const ffCols: GridColDef[] = [
    { field: 'columnNumber', headerName: '#', width: 55,
      renderCell: ({ value }: GridRenderCellParams) => value != null ? <Typography variant="caption" sx={{ fontFamily: 'monospace', fontWeight: 600 }}>{value}</Typography> : <Typography variant="caption" color="text.disabled">—</Typography> },
    { field: 'fieldAlias', headerName: 'Schema Field', flex: 1, minWidth: 120,
      renderCell: ({ value }: GridRenderCellParams) => <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'primary.main' }}>{value}</Typography> },
    { field: 'fieldName', headerName: 'Source Field', flex: 1, minWidth: 100 },
    { field: 'dataType', headerName: 'Src Type', width: 90,
      renderCell: ({ value }: GridRenderCellParams) => <Chip label={value} size="small" color={DATA_TYPE_COLOR[value] ?? 'default'} variant="outlined" /> },
    { field: 'dateFormat', headerName: 'Date Format', width: 130,
      renderCell: ({ value, row }: GridRenderCellParams) => row.dataType === 'DATE'
        ? (value ? <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'warning.main' }}>{value}</Typography>
                 : <Typography variant="caption" color="text.disabled">ISO-8601</Typography>)
        : <Typography variant="caption" color="text.disabled">—</Typography> },
    { field: '_a', headerName: '', width: readOnly ? 48 : 80, sortable: false,
      renderCell: ({ row }: GridRenderCellParams) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title={readOnly ? 'View' : 'Edit'}>
            <IconButton size="small" onClick={() => openEditFf(row._idx)}>
              {readOnly ? <VisibilityRoundedIcon fontSize="small" /> : <EditRoundedIcon fontSize="small" />}
            </IconButton>
          </Tooltip>
          {!readOnly && <Tooltip title="Remove"><IconButton size="small" color="error" onClick={() => setDeleteFfIdx(row._idx)}><DeleteRoundedIcon fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ) },
  ];

  const fileCols: GridColDef[] = [
    { field: 'filePath', headerName: 'Directory / Path', flex: 2, minWidth: 200,
      renderCell: ({ value }: GridRenderCellParams) => <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>{value}</Typography> },
    { field: 'description', headerName: 'Description', flex: 1.5, minWidth: 100 },
    { field: 'archiveDirectory', headerName: 'Archive Dir', flex: 1.5, minWidth: 120,
      renderCell: ({ value }: GridRenderCellParams) => value
        ? <Typography variant="caption" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{value}</Typography>
        : <Typography variant="caption" color="text.disabled">—</Typography> },
    { field: 'fields', headerName: 'Fields', width: 80, sortable: false,
      renderCell: ({ value }: GridRenderCellParams) => { const c = Array.isArray(value) ? value.length : 0; return c > 0 ? <Chip label={c} size="small" color="info" variant="outlined" /> : <Typography variant="caption" color="text.disabled">—</Typography>; } },
    { field: '_a', headerName: '', width: readOnly ? 48 : 80, sortable: false,
      renderCell: ({ row }: GridRenderCellParams) => (
        <Box sx={{ display: 'flex', gap: 0.5 }}>
          <Tooltip title={readOnly ? 'View' : 'Edit'}><IconButton size="small" onClick={() => openEditFile(row._idx)}><EditRoundedIcon fontSize="small" /></IconButton></Tooltip>
          {!readOnly && <Tooltip title="Remove"><IconButton size="small" color="error" onClick={() => setDeleteFileIdx(row._idx)}><DeleteRoundedIcon fontSize="small" /></IconButton></Tooltip>}
        </Box>
      ) },
  ];


  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;

  const title = readOnly ? 'View Data Source' : (isEdit ? 'Edit Data Source' : 'New Data Source');

  return (
    <>
      <PageHeader
        title={title}
        crumbs={[{ label: 'Data Sources', to: '/data-sources' }, { label: readOnly ? 'View' : isEdit ? 'Edit' : 'New' }]}
        action={readOnly && id ? { label: 'Edit', icon: <EditRoundedIcon />, onClick: () => navigate(`/data-sources/${id}/edit`) } : undefined}
      />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {readOnly && <Alert severity="info" sx={{ mb: 2 }}>Read-only view. Click <strong>Edit</strong> to make changes.</Alert>}

      <Card>
        <Tabs value={step} onChange={(_, v) => setStep(v)} variant="scrollable" scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}>
          <Tab label="1. Details" />
          <Tab label={`2. Ingestion (${files.length} files, webhook ${webhook.enabled ? 'enabled' : 'disabled'})`} />
          <Tab label="3. Review" />
        </Tabs>

        <CardContent sx={{ p: 3 }}>
          {step === 0 && (
            <>
              <Typography variant="subtitle1" fontWeight={600} gutterBottom>Source Details</Typography>
              <Grid container spacing={2.5} sx={{ mb: 3 }}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth required label="Name" value={source.name} disabled={readOnly}
                    onChange={(e) => setSource((s) => ({ ...s, name: e.target.value }))} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label="Description" value={source.description} disabled={readOnly}
                    onChange={(e) => setSource((s) => ({ ...s, description: e.target.value }))} />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth label="Destination Table" value={source.destinationTable} disabled={readOnly}
                    onChange={(e) => setSource((s) => ({ ...s, destinationTable: e.target.value }))}
                    helperText="Unique system-wide. SQL table where ingested rows are stored (e.g. transaction_events)"
                    slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              {/* Schema Fields */}
              <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 1 }}>
                <Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <TableChartRoundedIcon fontSize="small" color={schemaFields.length > 0 ? 'primary' : 'disabled'} />
                    <Typography variant="subtitle2" fontWeight={600}>
                      Schema Fields (Destination Columns)
                      {schemaFields.length > 0 && <Chip label={schemaFields.length} size="small" color="primary" sx={{ ml: 1, height: 18, fontSize: '0.7rem' }} />}
                    </Typography>
                  </Box>
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                    Define the destination table columns. These are the canonical field names used in rule
                    expressions. File and webhook field mappings reference these names.
                  </Typography>
                </Box>
                {!readOnly && (
                  <Button size="small" variant="outlined" startIcon={<AddRoundedIcon />} onClick={openAddSf}>
                    Add Field
                  </Button>
                )}
              </Box>
              <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, overflow: 'hidden' }}>
                <DataGrid
                  rows={schemaFields.map((f, i) => ({ ...f, _idx: i, id: i }))}
                  columns={sfCols} autoHeight disableRowSelectionOnClick
                  hideFooter={schemaFields.length <= 10} density="compact" sx={{ border: 0 }}
                  slots={{ noRowsOverlay: () => (
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', py: 3 }}>
                      <Typography variant="body2" color="text.secondary">No schema fields defined yet.</Typography>
                    </Box>
                  ) }}
                />
              </Box>
            </>
          )}

          {step === 1 && (
            <>
              {/* Files */}
              <Accordion defaultExpanded disableGutters elevation={0}
                sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, '&:before': { display: 'none' } }}>
                <AccordionSummary expandIcon={<ExpandMoreRoundedIcon />}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <FolderRoundedIcon fontSize="small" color="primary" />
                    <Typography fontWeight={600}>Watched Files
                      {files.length > 0 && <Chip label={files.length} size="small" color="primary" sx={{ ml: 1, height: 18, fontSize: '0.7rem' }} />}
                    </Typography>
                    {totalFields > 0 && <Typography variant="caption" color="text.secondary" sx={{ ml: 0.5 }}>— {totalFields} field{totalFields !== 1 ? 's' : ''} across all files</Typography>}
                  </Box>
                </AccordionSummary>
                <AccordionDetails sx={{ pt: 0 }}>
                  {!readOnly && (
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
                      <Button size="small" variant="outlined" startIcon={<AddRoundedIcon />} onClick={openAddFile}>Add File Path</Button>
                    </Box>
                  )}
                  <DataGrid rows={files.map((f, i) => ({ ...f, _idx: i, id: i }))} columns={fileCols}
                    autoHeight disableRowSelectionOnClick hideFooter={files.length <= 10} density="compact" sx={{ border: 0 }}
                    slots={{ noRowsOverlay: () => (<Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', py: 3 }}><Typography variant="body2" color="text.secondary">No file paths added yet.</Typography></Box>) }} />
                </AccordionDetails>
              </Accordion>
              <Box sx={{ mt: 2 }} />
              {/* Webhooks - Details View Only */}
              <Accordion disableGutters elevation={0}
                sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, '&:before': { display: 'none' } }}>
                <AccordionSummary expandIcon={<ExpandMoreRoundedIcon />}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <WebhookRoundedIcon fontSize="small" color={webhook.enabled ? 'secondary' : 'disabled'} />
                    <Typography fontWeight={600}>Webhook {webhook.enabled && <Chip label="Active" size="small" color="secondary" sx={{ ml: 1, height: 18, fontSize: '0.7rem' }} />}</Typography>
                  </Box>
                </AccordionSummary>
                <AccordionDetails sx={{ pt: 2 }}>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12 }}>
                      <Typography variant="caption" color="text.secondary">Webhook Path (Auto-generated)</Typography>
                      <TextField fullWidth disabled value={`/weeb-hook/${source.name || '(datasource-name)'}`} size="small" sx={{ mt: 0.5 }}
                        slotProps={{ input: { sx: { fontFamily: 'monospace', fontSize: '0.85rem' } } }} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <TextField fullWidth label="Description" value={webhook.description} disabled={readOnly}
                        onChange={(e) => setWebhook((w) => ({ ...w, description: e.target.value }))}
                        helperText="Optional description of what this webhook is used for" />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Box>
                          <Typography variant="caption" color="text.secondary">Status</Typography>
                          <Typography>{webhook.enabled ? 'Active (enabled)' : 'Inactive (disabled)'}</Typography>
                        </Box>
                        {!readOnly && (
                          <Box>
                            <Button variant={webhook.enabled ? 'outlined' : 'contained'} size="small"
                              onClick={() => setWebhook((w) => ({ ...w, enabled: !w.enabled }))}>
                              {webhook.enabled ? 'Disable' : 'Enable'}
                            </Button>
                          </Box>
                        )}
                      </Box>
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                      <Typography variant="caption" color="text.secondary">Sample Request Body</Typography>
                      {schemaFields.length > 0 ? (
                        <TextField
                          fullWidth
                          multiline
                          rows={6}
                          disabled
                          value={JSON.stringify(
                            Object.fromEntries(schemaFields.map(f => [f.name,
                              f.dataType === 'INTEGER' ? 123 :
                              f.dataType === 'DECIMAL' ? 123.45 :
                              f.dataType === 'BOOLEAN' ? true :
                              f.dataType === 'DATE' ? '2023-12-25' :
                              'example_value'
                            ])), null, 2
                          )}
                          size="small"
                          sx={{ mt: 0.5 }}
                          slotProps={{
                            input: { sx: { fontFamily: 'monospace', fontSize: '0.8rem' } },
                            htmlInput: { style: { resize: 'vertical' } }
                          }}
                        />
                      ) : (
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>Add schema fields to generate a sample request body.</Typography>
                      )}
                    </Grid>
                    <Grid size={{ xs: 12 }}>
                      <Typography variant="caption" color="text.secondary">Properties (mirrored from Schema Fields)</Typography>
                      {schemaFields.length > 0 ? (
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 0.5 }}>
                          {schemaFields.map((f, i) => (
                            <Chip key={i} label={`${f.name} (${f.dataType})`} size="small"
                              color={DATA_TYPE_COLOR[f.dataType] ?? 'default'} variant="filled"
                              sx={{ fontFamily: 'monospace', fontSize: '0.7rem' }} />
                          ))}
                        </Box>
                      ) : (
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>No schema fields defined. Add schema fields to populate webhook properties.</Typography>
                      )}
                    </Grid>
                  </Grid>
                </AccordionDetails>
              </Accordion>
            </>
          )}

          {step === 2 && (
            <Box>
              <Typography variant="subtitle1" fontWeight={600} gutterBottom>Review</Typography>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}><Typography variant="caption" color="text.secondary">Name</Typography><Typography>{source.name || '—'}</Typography></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><Typography variant="caption" color="text.secondary">Destination Table</Typography><Typography sx={{ fontFamily: 'monospace' }}>{source.destinationTable || '—'}</Typography></Grid>
                <Grid size={{ xs: 12 }}><Typography variant="caption" color="text.secondary">Description</Typography><Typography>{source.description || '—'}</Typography></Grid>
              </Grid>
              <Divider sx={{ my: 2 }} />
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 2 }}>
                <Chip label={`Schema Fields: ${schemaFields.length}`} color="primary" variant="outlined" />
                <Chip label={`Files: ${files.length}`} variant="outlined" />
                <Chip label={webhook.enabled ? 'Webhook: Active' : 'Webhook: Inactive'} color={webhook.enabled ? 'secondary' : 'default'} variant="outlined" />
                <Chip label={`File Fields: ${totalFields}`} variant="outlined" />
              </Box>
              {schemaFields.length > 0 && (
                <>
                  <Typography variant="subtitle2" color="text.secondary" gutterBottom>Schema Fields:</Typography>
                  <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mb: 2 }}>
                    {schemaFields.map((f, i) => (
                      <Chip key={i} label={`${f.name} (${f.dataType})`} size="small"
                        color={DATA_TYPE_COLOR[f.dataType] ?? 'default'} variant="filled"
                        sx={{ fontFamily: 'monospace', fontSize: '0.7rem' }} />
                    ))}
                  </Box>
                </>
              )}
            </Box>
          )}
        </CardContent>

        <Divider />
        <Box sx={{ p: 2, display: 'flex', gap: 1, justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button onClick={() => setStep((s) => Math.max(0, s - 1))} disabled={step === 0}>Back</Button>
            <Button onClick={() => setStep((s) => Math.min(2, s + 1))} disabled={step === 2 || !canGoNext}>Next</Button>
          </Box>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button onClick={() => navigate('/data-sources')}>{readOnly ? 'Close' : 'Cancel'}</Button>
            {!readOnly && (
              <Button variant="contained" startIcon={<SaveRoundedIcon />} onClick={handleSave} disabled={saving}>
                {saving ? 'Saving…' : 'Save'}
              </Button>
            )}
          </Box>
        </Box>
      </Card>

      {/* Schema Field Dialog */}
      {!readOnly && (
        <Dialog open={sfDialog} onClose={() => setSfDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>{editSfIdx !== null ? 'Edit Schema Field' : 'Add Schema Field'}</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 0.5 }}>
              <Grid size={{ xs: 12, sm: 7 }}>
                <TextField fullWidth required label="Name" value={sfForm.name}
                  onChange={(e) => setSfForm((p) => ({ ...p, name: e.target.value }))}
                  helperText="Unique per source. Destination column name (e.g. amount)"
                  slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
              </Grid>
              <Grid size={{ xs: 12, sm: 5 }}>
                <TextField fullWidth select required label="Target Data Type" value={sfForm.dataType}
                  onChange={(e) => setSfForm((p) => ({ ...p, dataType: e.target.value as FieldDataType }))}>
                  {DATA_TYPES.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
                </TextField>
              </Grid>
              <Grid size={{ xs: 12 }}>
                <TextField fullWidth multiline rows={2} label="Description (optional)"
                  value={sfForm.description ?? ''}
                  onChange={(e) => setSfForm((p) => ({ ...p, description: e.target.value }))}
                  helperText="Shown as hint in the rule-expression editor autocomplete" />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setSfDialog(false)}>Cancel</Button>
            <Button variant="contained" onClick={commitSf} disabled={!sfForm.name.trim()}>
              {editSfIdx !== null ? 'Update' : 'Add Field'}
            </Button>
          </DialogActions>
        </Dialog>
      )}
      <ConfirmDialog open={deleteSfIdx !== null} title="Remove Schema Field"
        message={`Remove "${schemaFields[deleteSfIdx!]?.name ?? 'this field'}"? File/webhook field mappings referencing it may break.`}
        onConfirm={() => { setSchemaFields((p) => p.filter((_, i) => i !== deleteSfIdx)); setDeleteSfIdx(null); }}
        onCancel={() => setDeleteSfIdx(null)} />

      {/* File Dialog */}
      <Dialog open={fileDialog} onClose={() => setFileDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>{readOnly ? 'View File' : (editFileIdx !== null ? 'Edit File' : 'Add File Path')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid size={{ xs: 12 }}>
              <TextField fullWidth required label="File Directory / Path" value={fileForm.filePath} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, filePath: e.target.value }))}
                helperText="Unique system-wide absolute path to the file or directory (e.g. /data/txn/feed.csv)"
                slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField fullWidth label="Description" value={fileForm.description ?? ''} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, description: e.target.value }))} />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField fullWidth label="Archive Directory" value={fileForm.archiveDirectory ?? ''} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, archiveDirectory: e.target.value }))}
                helperText="Optional. Absolute path to move successfully imported files into (e.g. /data/txn/archive). Files are renamed with a timestamp suffix."
                slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
          </Grid>
          <Typography variant="subtitle2" fontWeight={600} sx={{ mt: 2, mb: 1 }}>CSV Parsing Options</Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 6, sm: 3 }}>
              <TextField fullWidth label="Field Separator" value={fileForm.fieldSeparator ?? ''} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, fieldSeparator: e.target.value }))}
                helperText='Default: ","' slotProps={{ htmlInput: { maxLength: 4 }, input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <TextField fullWidth label="Quote Character" value={fileForm.quoteCharacter ?? ''} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, quoteCharacter: e.target.value }))}
                helperText='Default: &quot;"&quot;' slotProps={{ htmlInput: { maxLength: 4 }, input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <TextField fullWidth label="Line Separator" value={fileForm.lineSeparator ?? ''} disabled={readOnly}
                onChange={(e) => setFileForm((p) => ({ ...p, lineSeparator: e.target.value }))}
                helperText="Default: auto-detect" slotProps={{ htmlInput: { maxLength: 4 }, input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
            <Grid size={{ xs: 6, sm: 3 }}>
              <TextField fullWidth type="number" label="Skip First N Lines" disabled={readOnly}
                value={fileForm.skipFirstNLines ?? 0}
                onChange={(e) => setFileForm((p) => ({ ...p, skipFirstNLines: e.target.value ? Number(e.target.value) : 0 }))}
                helperText="Preamble lines before header" slotProps={{ htmlInput: { min: 0 } }} />
            </Grid>
          </Grid>
          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 1 }}>
            <Box>
              <Typography variant="subtitle2" fontWeight={600}>
                Field Mappings
                {(fileForm.fields?.length ?? 0) > 0 && <Chip label={fileForm.fields!.length} size="small" color="info" sx={{ ml: 1, height: 18, fontSize: '0.7rem' }} />}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Map source CSV columns to schema fields. Select the schema field name (prefilled from defined schema).
              </Typography>
            </Box>
            {!readOnly && <Button size="small" variant="outlined" startIcon={<AddRoundedIcon />} onClick={openAddFf}>Add Field</Button>}
          </Box>
          <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, overflow: 'hidden' }}>
            <DataGrid rows={(fileForm.fields ?? []).map((f, i) => ({ ...f, _idx: i, id: i }))} columns={ffCols}
              autoHeight disableRowSelectionOnClick hideFooter={(fileForm.fields?.length ?? 0) <= 10} density="compact" sx={{ border: 0 }}
              slots={{ noRowsOverlay: () => (<Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', py: 3 }}><Typography variant="body2" color="text.secondary">No field mappings yet.</Typography></Box>) }} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFileDialog(false)}>{readOnly ? 'Close' : 'Cancel'}</Button>
          {!readOnly && (
            <Button variant="contained" onClick={commitFile} disabled={!fileForm.filePath}>
              {editFileIdx !== null ? 'Update File' : 'Add File Path'}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* File Field Dialog */}
      <Dialog open={ffDialog} onClose={() => setFfDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{readOnly ? 'View Field Mapping' : (editFfIdx !== null ? 'Edit Field Mapping' : 'Add Field Mapping')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 0.5 }}>
            <Grid size={{ xs: 12, sm: 7 }}>
              {schemaFields.length > 0 ? (
                <TextField fullWidth select required label="Schema Field (Name)" value={ffForm.fieldAlias} disabled={readOnly}
                  onChange={(e) => setFfForm((p) => ({ ...p, fieldAlias: e.target.value, fieldName: p.fieldName || e.target.value }))}
                  helperText="Destination column this source column maps to">
                  {schemaFields.map((sf) => (
                    <MenuItem key={sf.name} value={sf.name}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography sx={{ fontFamily: 'monospace' }}>{sf.name}</Typography>
                        <Chip label={sf.dataType} size="small" color={DATA_TYPE_COLOR[sf.dataType] ?? 'default'} variant="outlined" sx={{ height: 16, fontSize: '0.65rem' }} />
                      </Box>
                    </MenuItem>
                  ))}
                </TextField>
              ) : (
                <TextField fullWidth required label="Schema Field Alias" value={ffForm.fieldAlias} disabled={readOnly}
                  onChange={(e) => setFfForm((p) => ({ ...p, fieldAlias: e.target.value, fieldName: p.fieldName || e.target.value }))}
                  helperText="Define schema fields on Step 1 for a dropdown" slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
              )}
            </Grid>
            <Grid size={{ xs: 12, sm: 5 }}>
              <TextField fullWidth label="Column Number" type="number" value={ffForm.columnNumber ?? ''} disabled={readOnly}
                onChange={(e) => setFfForm((p) => ({ ...p, columnNumber: e.target.value ? Number(e.target.value) : undefined }))}
                helperText="1-based column index" slotProps={{ htmlInput: { min: 1 } }} />
            </Grid>
            <Grid size={{ xs: 12, sm: 7 }}>
              <TextField fullWidth label="Source Field Name" value={ffForm.fieldName} disabled={readOnly}
                onChange={(e) => setFfForm((p) => ({ ...p, fieldName: e.target.value }))}
                helperText="CSV column header (auto-filled from schema field name)"
                slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
            </Grid>
            <Grid size={{ xs: 12, sm: 5 }}>
              <TextField fullWidth select required label="Source Data Type" value={ffForm.dataType} disabled={readOnly}
                onChange={(e) => setFfForm((p) => ({ ...p, dataType: e.target.value as FieldDataType, dateFormat: '' }))}>
                {DATA_TYPES.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
              </TextField>
            </Grid>
            {ffForm.dataType === 'DATE' && (
              <Grid size={{ xs: 12 }}>
                <TextField fullWidth label="Date Format" value={ffForm.dateFormat ?? ''} disabled={readOnly}
                  onChange={(e) => setFfForm((p) => ({ ...p, dateFormat: e.target.value }))}
                  helperText={readOnly
                    ? (!ffForm.dateFormat ? 'Using default ISO-8601 (yyyy-MM-dd)' : 'Java DateTimeFormatter pattern')
                    : 'Java DateTimeFormatter pattern — leave blank for ISO-8601 (yyyy-MM-dd). Examples: dd/MM/yyyy · MM-dd-yyyy · yyyyMMdd'}
                  slotProps={{ input: { sx: { fontFamily: 'monospace' } } }} />
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFfDialog(false)}>{readOnly ? 'Close' : 'Cancel'}</Button>
          {!readOnly && (
            <Button variant="contained" onClick={commitFf} disabled={!ffForm.fieldAlias || !ffForm.fieldName}>
              {editFfIdx !== null ? 'Update' : 'Add Mapping'}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      <ConfirmDialog open={deleteFfIdx !== null} title="Remove Field Mapping"
        message={`Remove mapping for "${(fileForm.fields ?? [])[deleteFfIdx!]?.fieldAlias ?? 'this field'}"?`}
        onConfirm={() => removeFf(deleteFfIdx!)} onCancel={() => setDeleteFfIdx(null)} />

      <ConfirmDialog open={deleteFileIdx !== null} title="Remove File Path"
        message={`Remove "${files[deleteFileIdx!]?.filePath ?? 'this path'}" and its field mappings?`}
        onConfirm={() => { setFiles((p) => p.filter((_, i) => i !== deleteFileIdx)); setDeleteFileIdx(null); }}
        onCancel={() => setDeleteFileIdx(null)} />
    </>
  );
}
