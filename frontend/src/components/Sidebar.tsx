import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box, List, ListItemButton, ListItemIcon, ListItemText, Typography, Divider,
} from '@mui/material';
import DashboardRoundedIcon     from '@mui/icons-material/DashboardRounded';
import StorageRoundedIcon       from '@mui/icons-material/StorageRounded';
import ArchiveRoundedIcon       from '@mui/icons-material/ArchiveRounded';
import TableViewRoundedIcon     from '@mui/icons-material/TableViewRounded';
import GavelRoundedIcon         from '@mui/icons-material/GavelRounded';
import MonitorHeartRoundedIcon  from '@mui/icons-material/MonitorHeartRounded';
import LoyaltyRoundedIcon       from '@mui/icons-material/LoyaltyRounded';

export const DRAWER_WIDTH = 240;

const NAV = [
  { label: 'Dashboard',        path: '/dashboard',        icon: <DashboardRoundedIcon /> },
  { label: 'Data Sources',     path: '/data-sources',     icon: <StorageRoundedIcon /> },
  { label: 'Archived Sources', path: '/archived-sources', icon: <ArchiveRoundedIcon /> },
  { label: 'Imported Data',    path: '/imported-data',    icon: <TableViewRoundedIcon /> },
  { label: 'Rules',            path: '/rules',            icon: <GavelRoundedIcon /> },
  { label: 'Monitor',          path: '/monitor',          icon: <MonitorHeartRoundedIcon /> },
];

export default function Sidebar() {
  const { pathname } = useLocation();
  const navigate = useNavigate();

  return (
    <Box sx={{ width: DRAWER_WIDTH, display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Logo */}
      <Box sx={{ px: 3, py: 2.5, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <LoyaltyRoundedIcon sx={{ color: 'primary.main', fontSize: 28 }} />
        <Typography variant="h6" sx={{ color: 'text.primary', letterSpacing: 0.5 }}>
          Loyalty Admin
        </Typography>
      </Box>

      <Divider sx={{ mx: 2 }} />

      {/* Navigation */}
      <Box sx={{ flex: 1, pt: 1, overflowY: 'auto' }}>
        <List disablePadding>
          {NAV.map(({ label, path, icon }) => (
            <ListItemButton
              key={path}
              selected={pathname === path || pathname.startsWith(path + '/')}
              onClick={() => navigate(path)}
            >
              <ListItemIcon>{icon}</ListItemIcon>
              <ListItemText primary={label} />
            </ListItemButton>
          ))}
        </List>
      </Box>

      {/* Footer */}
      <Divider sx={{ mx: 2 }} />
      <Box sx={{ px: 3, py: 2 }}>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
          v0.1.0
        </Typography>
      </Box>
    </Box>
  );
}
