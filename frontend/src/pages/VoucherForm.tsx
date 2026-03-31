import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, MenuItem, Button,
  Alert, CircularProgress, Tab, Tabs, Switch, FormControlLabel,
  Typography, Chip, Tooltip, IconButton,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import { DataGrid, type GridColDef } from '@mui/x-data-grid';
import SaveRoundedIcon      from '@mui/icons-material/SaveRounded';
import EmojiEventsRoundedIcon from '@mui/icons-material/EmojiEventsRounded';
import ContentCopyRoundedIcon from '@mui/icons-material/ContentCopyRounded';
import PageHeader from '../components/PageHeader';
import ConfirmDialog from '../components/ConfirmDialog';
import {
  getVoucher, createVoucher, updateVoucher,
  getVoucherInstances, awardVoucher,
} from '../api/vouchers';
import type { Voucher, VoucherInstance, VoucherType } from '../types';

const EMPTY_VOUCHER: Omit<Voucher, 'id' | 'archived' | 'instanceCount' | 'createdAt' | 'updatedAt'> = {
  name: '', code: '', description: '', voucherType: 'DISCOUNT', count: 100, active: true,
};

interface Props { readOnly?: boolean }

export default function VoucherForm({ readOnly = false }: Props) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id && !readOnly;
  const isView = !!id && readOnly;

  const [form, setForm]           = useState(EMPTY_VOUCHER);
  const [instances, setInstances] = useState<VoucherInstance[]>([]);
  const [tab, setTab]             = useState(0);
  const [loading, setLoading]     = useState(!!id);
  const [saving, setSaving]       = useState(false);
  const [awarding, setAwarding]   = useState(false);
  const [error, setError]         = useState('');
  const [awardConfirm, setAwardConfirm] = useState(false);
  const [copiedCode, setCopiedCode]     = useState<string | null>(null);

  const loadInstances = () =>
    getVoucherInstances(Number(id)).then((r) => setInstances(r.data)).catch(() => undefined);

  useEffect(() => {
    if (!id) return;
    getVoucher(Number(id))
      .then((r) => {
        const { id: _id, archived: _a, instanceCount: _ic, createdAt: _c, updatedAt: _u, ...rest } = r.data;
        setForm({ ...EMPTY_VOUCHER, ...rest });
        loadInstances();
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((p) => ({ ...p, [k]: e.target.value }));

  const handleSave = async () => {
    setSaving(true);
    setError('');
    try {
      if (isEdit) await updateVoucher(Number(id), form);
      else        await createVoucher(form);
      navigate('/vouchers');
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  const handleAward = async () => {
    setAwardConfirm(false);
    setAwarding(true);
    setError('');
    try {
      await awardVoucher(Number(id));
      loadInstances();
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Award failed — instance cap may have been reached');
    } finally {
      setAwarding(false);
    }
  };

  const handleCopy = (code: string) => {
    navigator.clipboard.writeText(code).then(() => {
      setCopiedCode(code);
      setTimeout(() => setCopiedCode(null), 2000);
    });
  };

  const instanceColumns: GridColDef<VoucherInstance>[] = [
    { field: 'id', headerName: 'ID', width: 80, type: 'number' },
    {
      field: 'secretCode', headerName: 'Secret Code', width: 160,
      renderCell: ({ value }) => (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
          <Box component="span" sx={{ fontFamily: 'monospace', fontWeight: 700,
            fontSize: '0.9rem', letterSpacing: 2, color: 'secondary.main' }}>
            {value}
          </Box>
          <Tooltip title={copiedCode === value ? 'Copied!' : 'Copy'}>
            <IconButton size="small" onClick={() => handleCopy(value)}>
              <ContentCopyRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Box>
      ),
    },
    {
      field: 'createdAt', headerName: 'Awarded At', flex: 1,
      valueFormatter: (value) => value ? new Date(value).toLocaleString() : '—',
    },
  ];

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  const title = isView ? 'View Voucher' : isEdit ? 'Edit Voucher' : 'New Voucher';
  const crumbs = [{ label: 'Vouchers', to: '/vouchers' }, { label: isView ? 'View' : isEdit ? 'Edit' : 'New' }];

  return (
    <>
      <PageHeader title={title} crumbs={crumbs} />

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <Tabs
          value={tab}
          onChange={(_, v) => setTab(v)}
          sx={{ borderBottom: 1, borderColor: 'divider', px: 2 }}
        >
          <Tab label="Details" />
          {!!id && <Tab label={`Awarded Instances (${instances.length})`} />}
        </Tabs>

        <CardContent sx={{ p: 3 }}>
          {/* ── Details tab ─────────────────────────────────────────────── */}
          {tab === 0 && (
            <Grid container spacing={3}>
              {/* Name */}
              <Grid size={{ xs: 12, sm: 7 }}>
                <TextField
                  fullWidth label="Name" required
                  value={form.name} onChange={set('name')}
                  inputProps={{ readOnly: isView }}
                />
              </Grid>

              {/* Voucher type */}
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  fullWidth select label="Type"
                  value={form.voucherType}
                  onChange={set('voucherType') as never}
                  inputProps={{ readOnly: isView }}
                >
                  {(['DISCOUNT'] as VoucherType[]).map((t) => (
                    <MenuItem key={t} value={t}>{t}</MenuItem>
                  ))}
                </TextField>
              </Grid>

              {/* Active toggle */}
              <Grid size={{ xs: 12, sm: 2 }} sx={{ display: 'flex', alignItems: 'center' }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={form.active}
                      onChange={(e) => !isView && setForm((p) => ({ ...p, active: e.target.checked }))}
                      color="success"
                      disabled={isView}
                    />
                  }
                  label={form.active ? 'Active' : 'Inactive'}
                />
              </Grid>

              {/* Code */}
              <Grid size={{ xs: 12, sm: 5 }}>
                <TextField
                  fullWidth label="Voucher Code" required
                  value={form.code} onChange={set('code')}
                  inputProps={{ readOnly: isView, style: { fontFamily: 'monospace', fontWeight: 600 } }}
                  helperText="Public identifier code for this voucher template"
                />
              </Grid>

              {/* Max instances */}
              <Grid size={{ xs: 12, sm: 3 }}>
                <TextField
                  fullWidth label="Max Codes (Count)" required
                  type="number" inputProps={{ min: 1, readOnly: isView }}
                  value={form.count}
                  onChange={(e) => !isView && setForm((p) => ({ ...p, count: Number(e.target.value) }))}
                  helperText="Maximum number of secret codes that can be awarded"
                />
              </Grid>

              {/* Description */}
              <Grid size={{ xs: 12 }}>
                <TextField
                  fullWidth label="Description" multiline rows={3}
                  value={form.description ?? ''} onChange={set('description')}
                  inputProps={{ readOnly: isView }}
                  helperText="Optional: terms, conditions, or purpose of the voucher"
                />
              </Grid>

              {/* Actions */}
              {!isView && (
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <Button
                      variant="contained" startIcon={<SaveRoundedIcon />}
                      onClick={handleSave} disabled={saving || !form.name || !form.code}
                    >
                      {saving ? 'Saving…' : isEdit ? 'Save Changes' : 'Create Voucher'}
                    </Button>
                    <Button variant="outlined" onClick={() => navigate('/vouchers')}>
                      Cancel
                    </Button>
                  </Box>
                </Grid>
              )}

              {isView && (
                <Grid size={{ xs: 12 }}>
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <Button variant="outlined" onClick={() => navigate(`/vouchers/${id}/edit`)}>
                      Edit
                    </Button>
                    <Button variant="outlined" onClick={() => navigate('/vouchers')}>
                      Back
                    </Button>
                  </Box>
                </Grid>
              )}
            </Grid>
          )}

          {/* ── Instances tab ────────────────────────────────────────────── */}
          {tab === 1 && !!id && (
            <Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    {instances.length} of {form.count} codes awarded
                  </Typography>
                  <Chip
                    label={form.active ? 'Active' : 'Inactive'}
                    size="small"
                    color={form.active ? 'success' : 'default'}
                  />
                </Box>
                <Button
                  variant="contained"
                  startIcon={<EmojiEventsRoundedIcon />}
                  onClick={() => setAwardConfirm(true)}
                  disabled={awarding || !form.active || instances.length >= form.count}
                  size="small"
                >
                  {awarding ? 'Awarding…' : 'Award New Code'}
                </Button>
              </Box>

              <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                <DataGrid
                  rows={instances} columns={instanceColumns} autoHeight
                  pageSizeOptions={[10, 25, 50]}
                  initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
                  disableRowSelectionOnClick sx={{ border: 0 }}
                  slots={{
                    noRowsOverlay: () => (
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center',
                        height: '100%', py: 3 }}>
                        <Typography variant="body2" color="text.secondary">
                          No codes awarded yet. Click "Award New Code" to generate one.
                        </Typography>
                      </Box>
                    ),
                  }}
                />
              </Box>
            </Box>
          )}
        </CardContent>
      </Card>

      <ConfirmDialog
        open={awardConfirm}
        title="Award Voucher Code"
        message={`Generate a new unique 7-character secret code for voucher "${form.name}" (${form.code})?`}
        onConfirm={handleAward}
        onCancel={() => setAwardConfirm(false)}
      />
    </>
  );
}

