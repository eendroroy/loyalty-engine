import { createTheme, alpha, type PaletteMode } from '@mui/material/styles';

// ── Palette extracted from the warm-autumn color image ───────────────────────
//
// Shared accent colours (mode-invariant)
const SOFT_RED   = '#C05848';   // dusty terracotta-red  — primary main (dark)
const WARM_RED   = '#D07060';   // lighter warm red      — primary light (dark)
const DEEP_RED   = '#8B3428';   // deep brick            — primary dark (dark)

const BRICK_RED  = '#B84038';   // more saturated for contrast on light bg — primary main (light)
const AMBER      = '#C8804A';   // warm amber-orange     — secondary (dark)
const RUST       = '#C07040';   // rust-amber            — secondary (light)

// Dark mode surfaces
const DARK_BG    = '#140804';   // page canvas
const DARK_PAPER = '#1E0C06';   // cards / dialogs
const DARK_SIDE  = '#0E0402';   // sidebar / appbar

// Light mode surfaces
const LIGHT_BG   = '#FBF4EE';   // warm cream canvas
const LIGHT_PAPER= '#FFF8F4';   // cards
const LIGHT_SIDE = '#F5EAE0';   // sidebar / appbar

// Text
const IVORY      = '#F5E8D8';   // dark-mode primary text
const MUTED_TAN  = '#B88C70';   // dark-mode secondary text
const DARK_INK   = '#280C06';   // light-mode primary text
const RUST_INK   = '#7A4030';   // light-mode secondary text

// ─────────────────────────────────────────────────────────────────────────────

export function createAppTheme(mode: PaletteMode) {
  const isDark = mode === 'dark';

  return createTheme({
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      h5: { fontWeight: 600 },
      h6: { fontWeight: 600 },
      subtitle2: { fontWeight: 500 },
    },

    palette: {
      mode,
      primary: isDark
        ? { main: SOFT_RED,  light: WARM_RED,  dark: DEEP_RED  }
        : { main: BRICK_RED, light: SOFT_RED,  dark: DEEP_RED  },
      secondary: isDark
        ? { main: AMBER, dark: '#A0602C', light: '#E0A870' }
        : { main: RUST,  dark: '#945028', light: '#D89060' },
      background: isDark
        ? { default: DARK_BG,   paper: DARK_PAPER }
        : { default: LIGHT_BG,  paper: LIGHT_PAPER },
      text: isDark
        ? { primary: IVORY,    secondary: MUTED_TAN }
        : { primary: DARK_INK, secondary: RUST_INK  },
      divider: isDark
        ? alpha(DEEP_RED, 0.55)
        : alpha(RUST, 0.28),
      success: { main: '#6daa71' },
      warning: { main: '#D4901E' },
      error:   { main: '#d94040' },
      info:    { main: '#5aa8c8' },
    },

    shape: { borderRadius: 10 },

    components: {
      // ── AppBar ──────────────────────────────────────────────────────────
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: isDark ? DARK_SIDE : LIGHT_SIDE,
            color: isDark ? IVORY : DARK_INK,
            boxShadow: isDark
              ? '0 2px 10px rgba(0,0,0,.60)'
              : '0 1px 6px rgba(180,80,60,.14)',
            borderBottom: `1px solid ${isDark
              ? alpha(DEEP_RED, 0.65)
              : alpha(RUST, 0.32)}`,
          },
        },
      },

      // ── Drawer / Sidebar ────────────────────────────────────────────────
      MuiDrawer: {
        styleOverrides: {
          paper: {
            backgroundColor: isDark ? DARK_SIDE : LIGHT_SIDE,
            backgroundImage: 'none',
            borderRight: `1px solid ${isDark
              ? alpha(DEEP_RED, 0.4)
              : alpha(RUST, 0.22)}`,
          },
        },
      },

      // ── Sidebar list items ──────────────────────────────────────────────
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            margin: '2px 8px',
            width: 'calc(100% - 16px)',
            '&.Mui-selected': {
              backgroundColor: isDark
                ? alpha(SOFT_RED, 0.20)
                : alpha(BRICK_RED, 0.12),
              borderLeft: `3px solid ${isDark ? AMBER : RUST}`,
              paddingLeft: '13px',
              '& .MuiListItemIcon-root': {
                color: isDark ? AMBER : RUST,
              },
              '& .MuiListItemText-primary': {
                color: isDark ? IVORY : DARK_INK,
                fontWeight: 600,
              },
              '&:hover': {
                backgroundColor: isDark
                  ? alpha(SOFT_RED, 0.30)
                  : alpha(BRICK_RED, 0.20),
              },
            },
            '&:hover': {
              backgroundColor: isDark
                ? alpha(IVORY, 0.05)
                : alpha(DARK_INK, 0.05),
            },
          },
        },
      },
      MuiListItemIcon: {
        styleOverrides: {
          root: {
            minWidth: 40,
            color: isDark ? alpha(IVORY, 0.40) : alpha(DARK_INK, 0.45),
          },
        },
      },
      MuiListItemText: {
        styleOverrides: {
          primary: {
            color: isDark ? alpha(IVORY, 0.80) : alpha(DARK_INK, 0.80),
            fontSize: '0.875rem',
          },
        },
      },

      // ── Cards ───────────────────────────────────────────────────────────
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: isDark
              ? '0 2px 14px rgba(0,0,0,.45)'
              : '0 2px 10px rgba(180,60,40,.10)',
            border: `1px solid ${isDark
              ? alpha(DEEP_RED, 0.40)
              : alpha(RUST, 0.28)}`,
            backgroundImage: 'none',
          },
        },
      },

      // ── Divider ─────────────────────────────────────────────────────────
      MuiDivider: {
        styleOverrides: {
          root: {
            borderColor: isDark ? alpha(DEEP_RED, 0.45) : alpha(RUST, 0.25),
          },
        },
      },

      // ── Inputs ──────────────────────────────────────────────────────────
      MuiOutlinedInput: {
        styleOverrides: {
          notchedOutline: {
            borderColor: isDark ? alpha(DEEP_RED, 0.55) : alpha(RUST, 0.45),
          },
          root: {
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: isDark ? alpha(SOFT_RED, 0.85) : alpha(BRICK_RED, 0.70),
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: isDark ? SOFT_RED : BRICK_RED,
            },
          },
        },
      },

      // ── Buttons ─────────────────────────────────────────────────────────
      MuiButton: {
        styleOverrides: {
          root: { textTransform: 'none', fontWeight: 500 },
          containedPrimary: {
            backgroundColor: isDark ? SOFT_RED : BRICK_RED,
            color: '#ffffff',
            '&:hover': {
              backgroundColor: isDark ? DEEP_RED : alpha(BRICK_RED, 0.84),
            },
          },
          containedSecondary: {
            backgroundColor: isDark ? AMBER : RUST,
            color: isDark ? '#0E0400' : '#ffffff',
            '&:hover': {
              backgroundColor: isDark ? alpha(AMBER, 0.84) : alpha(RUST, 0.84),
            },
          },
          outlinedPrimary: {
            borderColor: isDark ? alpha(SOFT_RED, 0.70) : alpha(BRICK_RED, 0.60),
            color: isDark ? SOFT_RED : BRICK_RED,
            '&:hover': {
              backgroundColor: isDark
                ? alpha(SOFT_RED, 0.10)
                : alpha(BRICK_RED, 0.08),
            },
          },
        },
      },

      // ── Chips ───────────────────────────────────────────────────────────
      MuiChip: {
        styleOverrides: { root: { fontWeight: 500, fontSize: '0.75rem' } },
      },

      // ── Table ───────────────────────────────────────────────────────────
      MuiTableCell: {
        styleOverrides: {
          head: {
            color: isDark ? alpha(IVORY, 0.58) : alpha(DARK_INK, 0.55),
            fontWeight: 600,
          },
        },
      },

      // ── Tabs ────────────────────────────────────────────────────────────
      MuiTab: {
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 500,
            color: isDark ? alpha(IVORY, 0.50) : alpha(DARK_INK, 0.50),
            '&.Mui-selected': { color: isDark ? AMBER : RUST },
          },
        },
      },
      MuiTabs: {
        styleOverrides: {
          indicator: {
            backgroundColor: isDark ? AMBER : RUST,
            height: 3,
            borderRadius: 2,
          },
        },
      },

      // ── Accordion ───────────────────────────────────────────────────────
      MuiAccordion: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: isDark ? DARK_PAPER : LIGHT_PAPER,
          },
        },
      },
      MuiAccordionSummary: {
        styleOverrides: {
          root: {
            '&:hover': {
              backgroundColor: isDark
                ? alpha(SOFT_RED, 0.07)
                : alpha(BRICK_RED, 0.06),
            },
          },
        },
      },

      // ── Tooltip ─────────────────────────────────────────────────────────
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            backgroundColor: isDark
              ? alpha(DARK_PAPER, 0.97)
              : alpha(LIGHT_PAPER, 0.97),
            border: `1px solid ${isDark ? alpha(AMBER, 0.40) : alpha(RUST, 0.40)}`,
            color: isDark ? IVORY : DARK_INK,
            fontSize: '0.75rem',
            boxShadow: '0 4px 16px rgba(0,0,0,.30)',
          },
        },
      },

      // ── Dialog ──────────────────────────────────────────────────────────
      MuiDialog: {
        styleOverrides: {
          paper: {
            backgroundColor: isDark ? DARK_PAPER : LIGHT_PAPER,
            backgroundImage: 'none',
            border: `1px solid ${isDark
              ? alpha(DEEP_RED, 0.45)
              : alpha(RUST, 0.35)}`,
            boxShadow: '0 8px 40px rgba(0,0,0,.45)',
          },
        },
      },

      // ── Paper (fallback) ─────────────────────────────────────────────────
      MuiPaper: {
        styleOverrides: { root: { backgroundImage: 'none' } },
      },
    },
  });
}

// Default export keeps backward-compat for any files that still import it directly
export default createAppTheme('dark');
