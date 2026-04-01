import { createTheme, alpha, type PaletteMode } from '@mui/material/styles';

// ── Dashboard Palette ───────────────────────────────────────────────────────
//
// ── Primary Reds ─────────────────────────────────────────────────────────────
const CRIMSON        = '#E53935';   // vivid crimson — primary brand
const CRIMSON_DARK   = '#B71C1C';   // deep red — hover / pressed
const CRIMSON_LIGHT  = '#EF9A9A';   // soft red — accents / tints
const CRIMSON_GLOW   = 'rgba(229, 57, 53, 0.35)'; // red shadow / glow

// ── Neutrals ──────────────────────────────────────────────────────────────────
// Dark
const D_BG        = '#0B0C12';   // main canvas — near-black navy
const D_SURFACE   = '#0F101A';   // sidebar surface
const D_PAPER     = '#14151F';   // card / panel surface
const D_ELEVATED  = '#1A1B28';   // elevated card / dialog
const D_BORDER    = 'rgba(255,255,255,0.06)';
const D_T1        = '#FFFFFF';
const D_T2        = '#94A3B8';   // slate-400
const D_T3        = '#475569';   // slate-600

// Light
const L_BG        = '#F1F4F9';   // soft blue-grey canvas
const L_SURFACE   = '#FFFFFF';   // sidebar
const L_PAPER     = '#FFFFFF';   // cards
const L_ELEVATED  = '#FFFFFF';
const L_BORDER    = 'rgba(15,23,42,0.08)';
const L_T1        = '#0F172A';   // slate-900
const L_T2        = '#64748B';   // slate-500
const L_T3        = '#94A3B8';   // slate-400

// ── Semantic ──────────────────────────────────────────────────────────────────
const SUCCESS = '#10B981';
const WARNING = '#F59E0B';
const INFO    = '#3B82F6';

// ─────────────────────────────────────────────────────────────────────────────

export function createAppTheme(mode: PaletteMode) {
  const dk = mode === 'dark';

  const accent   = CRIMSON;
  const bg       = dk ? D_BG      : L_BG;
  const surface  = dk ? D_SURFACE : L_SURFACE;
  const paper    = dk ? D_PAPER   : L_PAPER;
  const elevated = dk ? D_ELEVATED: L_ELEVATED;
  const border   = dk ? D_BORDER  : L_BORDER;
  const t1       = dk ? D_T1      : L_T1;
  const t2       = dk ? D_T2      : L_T2;

  return createTheme({
    // ── Typography ───────────────────────────────────────────────────────────
    typography: {
      fontFamily: '"Inter", "Segoe UI", "Roboto", "Helvetica Neue", sans-serif',
      h1: { fontWeight: 700, fontSize: '2rem',    letterSpacing: '-0.02em' },
      h2: { fontWeight: 700, fontSize: '1.75rem', letterSpacing: '-0.015em' },
      h3: { fontWeight: 600, fontSize: '1.5rem',  letterSpacing: '-0.01em' },
      h4: { fontWeight: 600, fontSize: '1.25rem', letterSpacing: '-0.01em' },
      h5: { fontWeight: 600, fontSize: '1.125rem' },
      h6: { fontWeight: 600, fontSize: '1rem' },
      subtitle1: { fontWeight: 500, fontSize: '0.9375rem' },
      subtitle2: { fontWeight: 500, fontSize: '0.8125rem' },
      body1: { fontSize: '0.875rem', lineHeight: 1.65 },
      body2: { fontSize: '0.8125rem', lineHeight: 1.6 },
      caption: { fontSize: '0.75rem', fontWeight: 400, letterSpacing: '0.01em' },
      button: { fontWeight: 600, textTransform: 'none', letterSpacing: '0.01em' },
    },

    // ── Palette ──────────────────────────────────────────────────────────────
    palette: {
      mode,
      primary:    { main: accent, dark: CRIMSON_DARK, light: CRIMSON_LIGHT, contrastText: '#fff' },
      secondary:  { main: INFO,   dark: '#1D4ED8',    light: '#60A5FA',     contrastText: '#fff' },
      success:    { main: SUCCESS, dark: '#059669', light: '#34D399', contrastText: '#fff' },
      warning:    { main: WARNING, dark: '#D97706', light: '#FCD34D', contrastText: '#fff' },
      error:      { main: CRIMSON, dark: CRIMSON_DARK, light: CRIMSON_LIGHT, contrastText: '#fff' },
      info:       { main: INFO,   dark: '#1D4ED8', light: '#60A5FA', contrastText: '#fff' },
      background: { default: bg, paper },
      text:       { primary: t1, secondary: t2, disabled: alpha(t2, 0.5) },
      divider:    border,
    },

    shape: { borderRadius: 12 },

    // ── Transitions — optimized for responsive feel ──────────────────────────
    transitions: {
      duration: {
        shortest: 100,
        shorter: 150,
        short: 200,
        standard: 250,
        complex: 300,
        enteringScreen: 200,
        leavingScreen: 150,
      },
      easing: {
        easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
        easeOut: 'cubic-bezier(0.0, 0, 0.2, 1)',
        easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
        sharp: 'cubic-bezier(0.4, 0, 0.6, 1)',
      },
    },

    // ── Shadows — custom depth scale ─────────────────────────────────────────
    shadows: [
      'none',
      dk ? '0 1px 4px rgba(0,0,0,.5)'  : '0 1px 3px rgba(15,23,42,.06)',
      dk ? '0 2px 8px rgba(0,0,0,.5)'  : '0 2px 6px rgba(15,23,42,.08)',
      dk ? '0 4px 16px rgba(0,0,0,.5)' : '0 4px 12px rgba(15,23,42,.1)',
      dk ? '0 6px 24px rgba(0,0,0,.5)' : '0 6px 18px rgba(15,23,42,.1)',
      dk ? '0 8px 32px rgba(0,0,0,.6)' : '0 8px 24px rgba(15,23,42,.12)',
      dk ? '0 12px 40px rgba(0,0,0,.6)' : '0 12px 32px rgba(15,23,42,.12)',
      dk ? '0 16px 48px rgba(0,0,0,.65)' : '0 16px 40px rgba(15,23,42,.14)',
      dk ? '0 20px 56px rgba(0,0,0,.7)' : '0 20px 48px rgba(15,23,42,.15)',
      ...Array(16).fill('none'),
    ] as any,

    components: {
      // ── Baseline ───────────────────────────────────────────────────────────
      MuiCssBaseline: {
        styleOverrides: {
          '*': { boxSizing: 'border-box' },
          'html, body': {
            scrollbarWidth: 'thin',
            scrollbarColor: dk ? `${D_T3} ${D_BG}` : `${L_T3} ${L_BG}`,
          },
          '::-webkit-scrollbar': { width: 6, height: 6 },
          '::-webkit-scrollbar-track': { background: dk ? D_BG : L_BG },
          '::-webkit-scrollbar-thumb': {
            background: dk ? D_T3 : L_T3,
            borderRadius: 3,
          },
          '::-webkit-scrollbar-thumb:hover': { background: accent },
        },
      },

      // ── AppBar ─────────────────────────────────────────────────────────────
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: dk ? alpha(D_PAPER, 0.92) : alpha(L_SURFACE, 0.94),
            backdropFilter: 'blur(16px) saturate(160%)',
            WebkitBackdropFilter: 'blur(16px) saturate(160%)',
            color: t1,
            borderBottom: `1px solid ${border}`,
            boxShadow: dk
              ? '0 1px 0 rgba(255,255,255,0.04), 0 4px 20px rgba(0,0,0,.4)'
              : '0 1px 0 rgba(15,23,42,0.06), 0 2px 8px rgba(15,23,42,.05)',
          },
        },
      },

      // ── Drawer / Sidebar ───────────────────────────────────────────────────
      MuiDrawer: {
        styleOverrides: {
          paper: {
            backgroundColor: surface,
            backgroundImage: 'none',
            borderRight: `1px solid ${border}`,
            boxShadow: dk
              ? '2px 0 24px rgba(0,0,0,.5)'
              : '2px 0 12px rgba(15,23,42,.06)',
          },
        },
      },

      // ── List / Nav items ───────────────────────────────────────────────────
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            margin: '2px 10px',
            width: 'calc(100% - 20px)',
            transition: 'all 0.12s cubic-bezier(0.4,0,0.2,1)',
            '&.Mui-selected': {
              backgroundColor: dk
                ? alpha(accent, 0.16)
                : alpha(accent, 0.09),
              boxShadow: dk
                ? `inset 3px 0 0 ${accent}, 0 2px 8px ${CRIMSON_GLOW}`
                : `inset 3px 0 0 ${accent}`,
              paddingLeft: '13px',
              '& .MuiListItemIcon-root': { color: accent },
              '& .MuiListItemText-primary': { color: dk ? '#fff' : L_T1, fontWeight: 600 },
              '&:hover': {
                backgroundColor: dk ? alpha(accent, 0.22) : alpha(accent, 0.13),
              },
            },
            '&:hover': {
              backgroundColor: dk ? alpha(D_T1, 0.05) : alpha(L_T1, 0.04),
            },
          },
        },
      },
      MuiListItemIcon: {
        styleOverrides: {
          root: {
            minWidth: 38,
            color: alpha(t2, 0.75),
            transition: 'color 0.12s',
          },
        },
      },
      MuiListItemText: {
        styleOverrides: {
          primary: { fontSize: '0.875rem', fontWeight: 500, color: alpha(t1, 0.88) },
        },
      },

      // ── Card ───────────────────────────────────────────────────────────────
      MuiCard: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: dk ? D_PAPER : L_PAPER,
            border: `1px solid ${border}`,
            boxShadow: dk
              ? '0 4px 24px rgba(0,0,0,.35), 0 1px 0 rgba(255,255,255,0.04)'
              : '0 2px 10px rgba(15,23,42,.07), 0 1px 3px rgba(15,23,42,.05)',
            transition: 'box-shadow 0.15s ease, transform 0.15s ease',
            '&:hover': {
              boxShadow: dk
                ? `0 8px 36px rgba(0,0,0,.5), 0 1px 0 rgba(255,255,255,0.06)`
                : '0 6px 20px rgba(15,23,42,.12)',
            },
          },
        },
      },

      // ── Paper ──────────────────────────────────────────────────────────────
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: dk ? D_PAPER : L_PAPER,
          },
          elevation1: {
            boxShadow: dk
              ? '0 2px 12px rgba(0,0,0,.4)'
              : '0 2px 8px rgba(15,23,42,.07)',
          },
          elevation2: {
            boxShadow: dk
              ? '0 4px 24px rgba(0,0,0,.5)'
              : '0 4px 16px rgba(15,23,42,.09)',
          },
        },
      },

      // ── Divider ────────────────────────────────────────────────────────────
      MuiDivider: {
        styleOverrides: {
          root: { borderColor: border },
        },
      },

      // ── OutlinedInput ──────────────────────────────────────────────────────
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            backgroundColor: dk ? alpha(D_BG, 0.6) : alpha(L_BG, 0.5),
            transition: 'box-shadow 0.12s',
            '& .MuiOutlinedInput-notchedOutline': {
              borderColor: alpha(t2, dk ? 0.2 : 0.25),
            },
            '&:hover .MuiOutlinedInput-notchedOutline': {
              borderColor: alpha(accent, 0.6),
            },
            '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
              borderColor: accent,
              borderWidth: 2,
            },
            '&.Mui-focused': {
              boxShadow: `0 0 0 3px ${alpha(accent, 0.12)}`,
            },
          },
        },
      },
      MuiInputLabel: {
        styleOverrides: {
          root: {
            color: alpha(t2, 0.8),
            '&.Mui-focused': { color: accent },
          },
        },
      },

      // ── Button ─────────────────────────────────────────────────────────────
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            fontWeight: 600,
            padding: '8px 18px',
            transition: 'all 0.12s cubic-bezier(0.4,0,0.2,1)',
          },
          containedPrimary: {
            background: `linear-gradient(135deg, ${accent} 0%, ${CRIMSON_DARK} 100%)`,
            boxShadow: `0 2px 12px ${CRIMSON_GLOW}`,
            '&:hover': {
              background: `linear-gradient(135deg, ${CRIMSON_DARK} 0%, #8B0000 100%)`,
              boxShadow: `0 4px 20px ${CRIMSON_GLOW}`,
              transform: 'translateY(-1px)',
            },
            '&:active': { transform: 'translateY(0)', boxShadow: `0 1px 6px ${CRIMSON_GLOW}` },
          },
          containedSecondary: {
            background: `linear-gradient(135deg, ${INFO} 0%, #1D4ED8 100%)`,
            boxShadow: '0 2px 12px rgba(59,130,246,.3)',
            '&:hover': {
              background: `linear-gradient(135deg, #1D4ED8 0%, #1E3A8A 100%)`,
              boxShadow: '0 4px 20px rgba(59,130,246,.35)',
              transform: 'translateY(-1px)',
            },
          },
          outlinedPrimary: {
            borderColor: alpha(accent, 0.6),
            color: accent,
            '&:hover': {
              backgroundColor: alpha(accent, 0.07),
              borderColor: accent,
              boxShadow: `0 0 0 3px ${alpha(accent, 0.1)}`,
            },
          },
          textPrimary: {
            color: accent,
            '&:hover': { backgroundColor: alpha(accent, 0.07) },
          },
        },
      },

      // ── IconButton ────────────────────────────────────────────────────────
      MuiIconButton: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            transition: 'all 0.12s',
            '&:hover': {
              backgroundColor: alpha(t2, 0.1),
              color: accent,
            },
          },
        },
      },

      // ── Chip ──────────────────────────────────────────────────────────────
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            fontWeight: 600,
            fontSize: '0.7rem',
            letterSpacing: '0.02em',
            height: 24,
          },
          colorPrimary: {
            backgroundColor: alpha(accent, dk ? 0.18 : 0.1),
            color: dk ? CRIMSON_LIGHT : accent,
          },
          colorSuccess: {
            backgroundColor: alpha(SUCCESS, dk ? 0.18 : 0.1),
            color: dk ? '#34D399' : '#059669',
          },
          colorWarning: {
            backgroundColor: alpha(WARNING, dk ? 0.18 : 0.1),
            color: dk ? '#FCD34D' : '#B45309',
          },
          colorError: {
            backgroundColor: alpha(CRIMSON, dk ? 0.18 : 0.1),
            color: dk ? CRIMSON_LIGHT : CRIMSON_DARK,
          },
          colorInfo: {
            backgroundColor: alpha(INFO, dk ? 0.18 : 0.1),
            color: dk ? '#60A5FA' : '#1D4ED8',
          },
        },
      },

      // ── TableCell ────────────────────────────────────────────────────────
      MuiTableCell: {
        styleOverrides: {
          head: {
            fontWeight: 600,
            fontSize: '0.75rem',
            letterSpacing: '0.04em',
            textTransform: 'uppercase',
            color: alpha(t2, 0.9),
            backgroundColor: dk ? D_ELEVATED : alpha(L_BG, 0.7),
            borderBottomColor: border,
          },
          body: {
            color: t1,
            borderBottomColor: border,
          },
        },
      },

      // ── Tabs ─────────────────────────────────────────────────────────────
      MuiTab: {
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 500,
            color: alpha(t2, 0.8),
            '&.Mui-selected': { color: accent, fontWeight: 600 },
          },
        },
      },
      MuiTabs: {
        styleOverrides: {
          indicator: { backgroundColor: accent, height: 3, borderRadius: '3px 3px 0 0' },
        },
      },

      // ── Accordion ────────────────────────────────────────────────────────
      MuiAccordion: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: dk ? D_ELEVATED : L_PAPER,
            border: `1px solid ${border}`,
            borderRadius: '12px !important',
            '&:before': { display: 'none' },
            '&.Mui-expanded': { margin: 0 },
          },
        },
      },

      // ── Tooltip ──────────────────────────────────────────────────────────
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            backgroundColor: dk ? alpha(D_ELEVATED, 0.97) : alpha(L_T1, 0.9),
            color: dk ? D_T1 : '#fff',
            fontSize: '0.75rem',
            fontWeight: 500,
            borderRadius: 8,
            padding: '6px 10px',
            boxShadow: dk
              ? '0 4px 16px rgba(0,0,0,.5)'
              : '0 4px 12px rgba(15,23,42,.2)',
          },
          arrow: {
            color: dk ? alpha(D_ELEVATED, 0.97) : alpha(L_T1, 0.9),
          },
        },
      },

      // ── Dialog ───────────────────────────────────────────────────────────
      MuiDialog: {
        styleOverrides: {
          paper: {
            backgroundColor: dk ? D_ELEVATED : L_PAPER,
            backgroundImage: 'none',
            border: `1px solid ${border}`,
            borderRadius: 16,
            boxShadow: dk
              ? '0 24px 64px rgba(0,0,0,.7)'
              : '0 24px 64px rgba(15,23,42,.18)',
          },
        },
      },

      // ── Select ───────────────────────────────────────────────────────────
      MuiSelect: {
        styleOverrides: {
          icon: { color: alpha(t2, 0.7) },
        },
      },
      MuiMenuItem: {
        styleOverrides: {
          root: {
            fontSize: '0.875rem',
            transition: 'background-color 0.1s ease',
            '&:hover': { backgroundColor: alpha(accent, 0.07) },
            '&.Mui-selected': {
              backgroundColor: alpha(accent, dk ? 0.2 : 0.09),
              fontWeight: 600,
              '&:hover': { backgroundColor: alpha(accent, dk ? 0.25 : 0.13) },
            },
          },
        },
      },

      // ── Menu / Dropdown ─────────────────────────────────────────────────
      MuiMenu: {
        styleOverrides: {
          paper: {
            boxShadow: dk
              ? '0 8px 32px rgba(0,0,0,.6)'
              : '0 8px 24px rgba(15,23,42,.15)',
            border: `1px solid ${border}`,
            borderRadius: 10,
          },
        },
      },
      MuiPopover: {
        styleOverrides: {
          paper: {
            borderRadius: 10,
            border: `1px solid ${border}`,
          },
        },
      },


      // ── ToggleButton ─────────────────────────────────────────────────────
      MuiToggleButton: {
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 500,
            fontSize: '0.8125rem',
            borderColor: border,
            color: t2,
            borderRadius: '10px !important',
            '&.Mui-selected': {
              backgroundColor: alpha(accent, dk ? 0.2 : 0.1),
              color: accent,
              borderColor: alpha(accent, 0.4),
              fontWeight: 600,
              '&:hover': { backgroundColor: alpha(accent, dk ? 0.25 : 0.15) },
            },
            '&:hover': { backgroundColor: alpha(t2, 0.07) },
          },
        },
      },
      MuiToggleButtonGroup: {
        styleOverrides: {
          root: { gap: 4 },
        },
      },

      // ── Alert ────────────────────────────────────────────────────────────
      MuiAlert: {
        styleOverrides: {
          root: {
            borderRadius: 10,
            fontSize: '0.875rem',
          },
        },
      },

    },
  });
}

// Light is default
export default createAppTheme('light');
