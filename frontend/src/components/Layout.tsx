import { useState } from 'react';
import { useLocation } from 'react-router-dom';
import {
  Box, AppBar, Toolbar, IconButton, Typography, Drawer,
  useTheme, useMediaQuery, Avatar, Tooltip,
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
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const { pathname } = useLocation();
  const { mode, toggleColorMode } = useColorMode();

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* ── App Bar ─────────────────────────────────────────────────────── */}
      <AppBar position="fixed" color="default" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          {isMobile && (
            <IconButton edge="start" sx={{ mr: 1 }} onClick={() => setMobileOpen(true)}>
              <MenuRoundedIcon />
            </IconButton>
          )}
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 600, color: 'primary.main' }}>
            {getTitle(pathname)}
          </Typography>

          {/* Dark / Light mode toggle */}
          <Tooltip title={mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}>
            <IconButton
              size="small"
              onClick={toggleColorMode}
              sx={{
                mr: 1,
                color: 'text.secondary',
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: 2,
                px: 0.8,
                py: 0.4,
                transition: 'all 0.2s',
                '&:hover': { color: 'primary.main', borderColor: 'primary.main' },
              }}
            >
              {mode === 'dark'
                ? <LightModeRoundedIcon fontSize="small" />
                : <DarkModeRoundedIcon  fontSize="small" />}
            </IconButton>
          </Tooltip>

          <Tooltip title="Notifications">
            <IconButton size="small" sx={{ mr: 1 }}>
              <NotificationsRoundedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Avatar sx={{ width: 34, height: 34, bgcolor: 'primary.main', fontSize: '0.85rem' }}>
            LA
          </Avatar>
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
          pb: 4,
          px: { xs: 2, sm: 3, md: 4 },
        }}
      >
        {children}
      </Box>
    </Box>
  );
}
