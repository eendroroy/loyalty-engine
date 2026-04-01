import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box, List, ListItemButton, ListItemIcon, ListItemText,
  Typography, Divider, useTheme, alpha, Toolbar,
} from '@mui/material';
import DashboardRoundedIcon         from '@mui/icons-material/DashboardRounded';
import StorageRoundedIcon           from '@mui/icons-material/StorageRounded';
import ArchiveRoundedIcon           from '@mui/icons-material/ArchiveRounded';
import TableViewRoundedIcon         from '@mui/icons-material/TableViewRounded';
import GavelRoundedIcon             from '@mui/icons-material/GavelRounded';
import MonitorHeartRoundedIcon      from '@mui/icons-material/MonitorHeartRounded';
import LoyaltyRoundedIcon           from '@mui/icons-material/LoyaltyRounded';
import ConfirmationNumberRoundedIcon from '@mui/icons-material/ConfirmationNumberRounded';

export const DRAWER_WIDTH = 240;

const NAV = [
  { label: 'Dashboard',        path: '/dashboard',        icon: <DashboardRoundedIcon fontSize="small" /> },
  { label: 'Data Sources',     path: '/data-sources',     icon: <StorageRoundedIcon fontSize="small" /> },
  { label: 'Archived Sources', path: '/archived-sources', icon: <ArchiveRoundedIcon fontSize="small" /> },
  { label: 'Imported Data',    path: '/imported-data',    icon: <TableViewRoundedIcon fontSize="small" /> },
  { label: 'Rules',            path: '/rules',            icon: <GavelRoundedIcon fontSize="small" /> },
  { label: 'Vouchers',         path: '/vouchers',         icon: <ConfirmationNumberRoundedIcon fontSize="small" /> },
  { label: 'Monitor',          path: '/monitor',          icon: <MonitorHeartRoundedIcon fontSize="small" /> },
];

export default function Sidebar() {
  const { pathname } = useLocation();
  const navigate     = useNavigate();
  const theme        = useTheme();
  const isDark       = theme.palette.mode === 'dark';

  return (
    <Box
      sx={{
        width: DRAWER_WIDTH,
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        bgcolor: 'background.paper',
      }}
    >
      {/* ── AppBar Spacer ─────────────────────────────────────────────────── */}
      <Toolbar />

      {/* ── Logo / Brand ──────────────────────────────────────────────────── */}
      <Box
        sx={{
          px: 2.5,
          pt: 2.5,
          pb: 2,
          display: 'flex',
          alignItems: 'center',
          gap: 1.25,
        }}
      >
        <Box
          sx={{
            width: 36,
            height: 36,
            borderRadius: '10px',
            background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: isDark
              ? `0 4px 12px rgba(229,57,53,0.4)`
              : `0 4px 12px rgba(229,57,53,0.25)`,
            flexShrink: 0,
          }}
        >
          <LoyaltyRoundedIcon sx={{ color: '#fff', fontSize: 20 }} />
        </Box>
        <Box>
          <Typography
            variant="subtitle1"
            sx={{
              fontWeight: 700,
              lineHeight: 1.2,
              color: 'text.primary',
              letterSpacing: '-0.01em',
            }}
          >
            Loyalty
          </Typography>
          <Typography variant="caption" sx={{ color: 'text.secondary', lineHeight: 1 }}>
            Admin Console
          </Typography>
        </Box>
      </Box>

      <Divider sx={{ mx: 2, mb: 1 }} />

      {/* ── Navigation ────────────────────────────────────────────────────── */}
      <Box sx={{ flex: 1, overflowY: 'auto', py: 0.5 }}>
        <Typography
          variant="caption"
          sx={{
            px: 2.5,
            py: 1,
            display: 'block',
            color: 'text.secondary',
            fontWeight: 600,
            letterSpacing: '0.06em',
            textTransform: 'uppercase',
            opacity: 0.6,
          }}
        >
          Main Menu
        </Typography>

        <List disablePadding>
          {NAV.map(({ label, path, icon }) => {
            const isActive = pathname === path || pathname.startsWith(path + '/');
            return (
              <ListItemButton
                key={path}
                selected={isActive}
                onClick={() => navigate(path)}
                sx={{ py: 1 }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive ? 'primary.main' : alpha(theme.palette.text.secondary, 0.7),
                  }}
                >
                  {icon}
                </ListItemIcon>
                <ListItemText primary={label} />
              </ListItemButton>
            );
          })}
        </List>
      </Box>

      {/* ── Footer ────────────────────────────────────────────────────────── */}
      <Divider sx={{ mx: 2 }} />
      <Box
        sx={{
          px: 2.5,
          py: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Typography variant="caption" sx={{ color: 'text.secondary', opacity: 0.6 }}>
          © 2026 Loyalty
        </Typography>
        <Box
          sx={{
            px: 1,
            py: 0.25,
            borderRadius: 1,
            bgcolor: alpha(theme.palette.primary.main, isDark ? 0.18 : 0.09),
            color: 'primary.main',
          }}
        >
          <Typography variant="caption" sx={{ fontWeight: 700, fontSize: '0.65rem', letterSpacing: '0.04em' }}>
            v0.3.0
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
