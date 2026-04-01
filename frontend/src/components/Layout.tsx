import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Box, AppBar, Toolbar, IconButton, Typography, Drawer,
  useTheme, useMediaQuery, Avatar, Tooltip, alpha, Badge,
} from '@mui/material';
import MenuRoundedIcon          from '@mui/icons-material/MenuRounded';
import NotificationsRoundedIcon from '@mui/icons-material/NotificationsRounded';
import DarkModeRoundedIcon      from '@mui/icons-material/DarkModeRounded';
import LightModeRoundedIcon     from '@mui/icons-material/LightModeRounded';
import Sidebar, { DRAWER_WIDTH } from './Sidebar';
import { useColorMode } from '../ColorModeContext';

const TITLES: Record<string, string> = {
  '/dashboard':        'Dashboard',
  '/data-sources':     'Data Sources',
  '/archived-sources': 'Archived Sources',
  '/imported-data':    'Imported Data',
  '/rules':            'Rules',
  '/vouchers':         'Vouchers',
  '/monitor':          'Monitor',
};

function getTitle(pathname: string) {
  for (const [key, val] of Object.entries(TITLES)) {
    if (pathname === key || pathname.startsWith(key + '/')) return val;
  }
  return 'Loyalty Admin';
}

interface Props { children: React.ReactNode }

export default function Layout({ children }: Props) {
  const theme    = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const isDark   = theme.palette.mode === 'dark';
  const [mobileOpen, setMobileOpen] = useState(false);
  const { pathname }                = useLocation();
  const { mode, toggleColorMode }   = useColorMode();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>

      {/* ── App Bar ───────────────────────────────────────────────────────── */}
      <AppBar position="fixed" color="default" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar sx={{ gap: 1, minHeight: { xs: 56, sm: 60 } }}>
          {isMobile && (
            <IconButton
              edge="start"
              size="small"
              sx={{ color: 'text.secondary' }}
              onClick={() => setMobileOpen(true)}
            >
              <MenuRoundedIcon fontSize="small" />
            </IconButton>
          )}

          {/* Page title */}
          <Typography
            variant="h6"
            sx={{
              flexGrow: 1,
              fontWeight: 700,
              color: 'text.primary',
              letterSpacing: '-0.01em',
              fontSize: '1rem',
            }}
          >
            {getTitle(pathname)}
          </Typography>

          {/* Theme toggle */}
          <Tooltip title={isDark ? 'Switch to light mode' : 'Switch to dark mode'} arrow>
            <IconButton
              size="small"
              onClick={toggleColorMode}
              sx={{
                color: 'text.secondary',
                border: `1px solid`,
                borderColor: 'divider',
                borderRadius: '10px',
                width: 34,
                height: 34,
                transition: 'all 0.18s',
                '&:hover': {
                  color: 'primary.main',
                  borderColor: 'primary.main',
                  bgcolor: alpha(theme.palette.primary.main, 0.07),
                },
              }}
            >
              {isDark
                ? <LightModeRoundedIcon sx={{ fontSize: 16 }} />
                : <DarkModeRoundedIcon  sx={{ fontSize: 16 }} />}
            </IconButton>
          </Tooltip>

          {/* Notifications */}
          <Tooltip title="Notifications" arrow>
            <IconButton
              size="small"
              sx={{
                color: 'text.secondary',
                width: 34,
                height: 34,
                '&:hover': { color: 'primary.main', bgcolor: alpha(theme.palette.primary.main, 0.07) },
              }}
            >
              <Badge
                badgeContent={3}
                color="error"
                sx={{
                  '& .MuiBadge-badge': {
                    width: 16,
                    height: 16,
                    minWidth: 16,
                    fontSize: '0.6rem',
                    padding: 0,
                  },
                }}
              >
                <NotificationsRoundedIcon sx={{ fontSize: 18 }} />
              </Badge>
            </IconButton>
          </Tooltip>

          {/* Avatar */}
          <Tooltip title="Admin User" arrow>
            <Avatar
              sx={{
                width: 32,
                height: 32,
                background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
                fontSize: '0.75rem',
                fontWeight: 700,
                cursor: 'pointer',
                boxShadow: isDark
                  ? '0 2px 8px rgba(229,57,53,.4)'
                  : '0 2px 8px rgba(229,57,53,.25)',
                transition: 'transform 0.18s',
                '&:hover': { transform: 'scale(1.08)' },
              }}
            >
              LA
            </Avatar>
          </Tooltip>
        </Toolbar>
      </AppBar>

      {/* ── Sidebar — permanent on desktop ─────────────────────────────── */}
      {!isMobile && (
        <Drawer variant="permanent" sx={{ width: DRAWER_WIDTH, flexShrink: 0 }}>
          <Sidebar />
        </Drawer>
      )}

      {/* ── Sidebar — temporary on mobile ──────────────────────────────── */}
      {isMobile && (
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}
        >
          <Sidebar />
        </Drawer>
      )}

      {/* ── Main content ────────────────────────────────────────────────── */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          bgcolor: 'background.default',
          pt: { xs: 9, md: 10 },
          pb: 5,
          px: { xs: 2, sm: 3, md: 4 },
        }}
      >
        {children}
      </Box>
    </Box>
  );
}
