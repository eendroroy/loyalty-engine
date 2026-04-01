import { Box, Typography, Breadcrumbs, Link, Button, useTheme, alpha } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import NavigateNextRoundedIcon from '@mui/icons-material/NavigateNextRounded';

interface Crumb { label: string; to?: string }

interface Props {
  title: string;
  subtitle?: string;
  crumbs?: Crumb[];
  action?: { label: string; icon?: React.ReactNode; onClick: () => void };
  secondaryAction?: { label: string; icon?: React.ReactNode; onClick: () => void };
}

export default function PageHeader({ title, subtitle, crumbs, action, secondaryAction }: Props) {
  const theme  = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const accent = theme.palette.primary.main;

  return (
    <Box
      sx={{
        mb: 3,
        display: 'flex',
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: 2,
      }}
    >
      <Box>
        {crumbs && crumbs.length > 0 && (
          <Breadcrumbs
            separator={<NavigateNextRoundedIcon sx={{ fontSize: 14, color: 'text.secondary', opacity: 0.5 }} />}
            sx={{ mb: 0.75 }}
          >
            {crumbs.map((c, i) =>
              c.to ? (
                <Link
                  key={i}
                  component={RouterLink}
                  to={c.to}
                  underline="hover"
                  sx={{
                    fontSize: '0.78rem',
                    fontWeight: 500,
                    color: 'text.secondary',
                    '&:hover': { color: accent },
                  }}
                >
                  {c.label}
                </Link>
              ) : (
                <Typography key={i} sx={{ fontSize: '0.78rem', fontWeight: 500, color: 'text.primary' }}>
                  {c.label}
                </Typography>
              ),
            )}
          </Breadcrumbs>
        )}

        <Typography
          variant="h5"
          sx={{
            fontWeight: 700,
            letterSpacing: '-0.02em',
            color: 'text.primary',
            lineHeight: 1.2,
          }}
        >
          {title}
        </Typography>

        {subtitle && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ mt: 0.5, lineHeight: 1.5 }}
          >
            {subtitle}
          </Typography>
        )}
      </Box>

      {(action || secondaryAction) && (
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
          {secondaryAction && (
            <Button
              variant="outlined"
              startIcon={secondaryAction.icon}
              onClick={secondaryAction.onClick}
              size="small"
              sx={{
                borderColor: isDark ? alpha('#fff', 0.12) : alpha('#000', 0.1),
                color: 'text.primary',
                '&:hover': {
                  borderColor: accent,
                  bgcolor: alpha(accent, 0.06),
                  color: accent,
                },
              }}
            >
              {secondaryAction.label}
            </Button>
          )}
          {action && (
            <Button
              variant="contained"
              startIcon={action.icon}
              onClick={action.onClick}
              size="small"
            >
              {action.label}
            </Button>
          )}
        </Box>
      )}
    </Box>
  );
}
