import { useState } from 'react';
import {
  Box, TextField, Typography, Chip, Collapse, IconButton,
  InputAdornment, Tooltip, Divider,
} from '@mui/material';
import ExpandMoreRoundedIcon  from '@mui/icons-material/ExpandMoreRounded';
import ExpandLessRoundedIcon  from '@mui/icons-material/ExpandLessRounded';
import SearchRoundedIcon      from '@mui/icons-material/SearchRounded';
import ContentCopyRoundedIcon from '@mui/icons-material/ContentCopyRounded';
import StorageRoundedIcon     from '@mui/icons-material/StorageRounded';
import type { Metadata } from '../types';

const DATA_TYPE_COLOR: Record<string, 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error'> = {
  STRING:  'default',
  INTEGER: 'primary',
  DECIMAL: 'secondary',
  DATE:    'warning',
  BOOLEAN: 'error',
};

interface Props {
  metadata: Metadata | null;
  /** Called with the alias string when the user clicks a field chip. */
  onInsert: (alias: string) => void;
}

export default function FieldBrowser({ metadata, onInsert }: Props) {
  const [open, setOpen]     = useState(true);
  const [search, setSearch] = useState('');
  const [copied, setCopied] = useState<string | null>(null);

  const q = search.trim().toLowerCase();

  const filtered = (metadata?.dataSources ?? [])
    .map((ds) => ({
      ...ds,
      fields: ds.fields.filter(
        (f) =>
          f.alias.toLowerCase().includes(q) ||
          f.fieldName.toLowerCase().includes(q) ||
          f.dataType.toLowerCase().includes(q),
      ),
    }))
    .filter((ds) => ds.fields.length > 0);

  const totalShown = filtered.reduce((acc, ds) => acc + ds.fields.length, 0);

  const handleCopy = (alias: string) => {
    navigator.clipboard.writeText(alias).catch(() => undefined);
    setCopied(alias);
    setTimeout(() => setCopied(null), 1500);
  };

  return (
    <Box
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        overflow: 'hidden',
      }}
    >
      {/* ── Header ────────────────────────────────────────────────────────── */}
      <Box
        onClick={() => setOpen((v) => !v)}
        sx={{
          px: 2, py: 1.25,
          display: 'flex', alignItems: 'center', gap: 1,
          bgcolor: 'background.default',
          cursor: 'pointer',
          userSelect: 'none',
          '&:hover': { bgcolor: 'action.hover' },
        }}
      >
        <StorageRoundedIcon fontSize="small" sx={{ color: 'primary.main' }} />
        <Typography variant="subtitle2" sx={{ flex: 1 }}>
          Available Fields
          <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 1 }}>
            {metadata ? `${metadata.totalFields} total` : '—'}
          </Typography>
        </Typography>
        <IconButton size="small" tabIndex={-1}>
          {open ? <ExpandLessRoundedIcon fontSize="small" /> : <ExpandMoreRoundedIcon fontSize="small" />}
        </IconButton>
      </Box>

      <Collapse in={open}>
        <Divider />

        {/* ── Search ──────────────────────────────────────────────────────── */}
        <Box sx={{ px: 2, pt: 1.5, pb: 1 }}>
          <TextField
            size="small" fullWidth
            placeholder="Search by alias, field name or type…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
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

        {/* ── Field groups ────────────────────────────────────────────────── */}
        <Box sx={{ px: 2, pb: 1.5, maxHeight: 340, overflowY: 'auto' }}>
          {filtered.length === 0 && (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2, textAlign: 'center' }}>
              {metadata ? 'No fields match your search.' : 'No data sources configured yet.'}
            </Typography>
          )}

          {filtered.map((ds) => (
            <Box key={ds.id} sx={{ mb: 1.5 }}>
              {/* datasource label */}
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 0.75 }}>
                <Typography variant="caption" fontWeight={600} color="text.secondary">
                  {ds.name}
                </Typography>
                <Typography variant="caption" color="text.disabled">
                  ({ds.fields.length})
                </Typography>
              </Box>

              {/* field chips */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
                {ds.fields.map((f) => (
                    <Tooltip
                      key={f.alias}
                      title={
                        <Box>
                          <div><strong>Alias:</strong> {f.alias}</div>
                          <div><strong>Field:</strong> {f.fieldName}</div>
                          <div><strong>Type:</strong> {f.dataType}</div>
                          {f.columnNumber != null && <div><strong>Column:</strong> {f.columnNumber}</div>}
                          {f.description && (
                            <div style={{ marginTop: 4, color: '#e0e0e0' }}>{f.description}</div>
                          )}
                          <div style={{ marginTop: 4, color: '#90caf9' }}>Click to insert · copy icon to copy</div>
                        </Box>
                      }
                      arrow
                    >
                    <Box sx={{ display: 'inline-flex', alignItems: 'center', gap: 0 }}>
                      <Chip
                        label={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                            <span style={{ fontFamily: 'monospace', fontSize: '0.78rem' }}>{f.alias}</span>
                            <Chip
                              label={f.dataType}
                              size="small"
                              color={DATA_TYPE_COLOR[f.dataType] ?? 'default'}
                              sx={{ height: 14, fontSize: '0.6rem', ml: 0.25 }}
                            />
                          </Box>
                        }
                        onClick={() => onInsert(f.alias)}
                        onDelete={() => handleCopy(f.alias)}
                        deleteIcon={
                          <Tooltip title={copied === f.alias ? 'Copied!' : 'Copy alias'}>
                            <ContentCopyRoundedIcon sx={{ fontSize: '0.75rem !important' }} />
                          </Tooltip>
                        }
                        variant="outlined"
                        size="small"
                        sx={{
                          borderRadius: 1,
                          '&:hover': { bgcolor: 'primary.main', color: 'white',
                            '& .MuiChip-deleteIcon': { color: 'rgba(255,255,255,0.7)' } },
                          ...(copied === f.alias && { bgcolor: 'success.light' }),
                        }}
                      />
                    </Box>
                  </Tooltip>
                ))}
              </Box>
            </Box>
          ))}

          {q && totalShown > 0 && (
            <Typography variant="caption" color="text.disabled">
              Showing {totalShown} of {metadata?.totalFields ?? 0} fields
            </Typography>
          )}
        </Box>
      </Collapse>
    </Box>
  );
}

