import { Box, Typography, Breadcrumbs, Link, Button } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

interface Crumb { label: string; to?: string }

interface Props {
  title: string;
  crumbs?: Crumb[];
  action?: { label: string; icon?: React.ReactNode; onClick: () => void };
}

export default function PageHeader({ title, crumbs, action }: Props) {
  return (
    <Box sx={{ mb: 3, display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
      <Box>
        {crumbs && crumbs.length > 0 && (
          <Breadcrumbs sx={{ mb: 0.5 }}>
            {crumbs.map((c, i) =>
              c.to ? (
                <Link key={i} component={RouterLink} to={c.to} underline="hover" color="inherit" variant="body2">
                  {c.label}
                </Link>
              ) : (
                <Typography key={i} variant="body2" color="text.primary">{c.label}</Typography>
              ),
            )}
          </Breadcrumbs>
        )}
        <Typography variant="h5">{title}</Typography>
      </Box>
      {action && (
        <Button variant="contained" startIcon={action.icon} onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </Box>
  );
}

