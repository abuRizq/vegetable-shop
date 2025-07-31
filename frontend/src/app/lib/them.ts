// theme.ts
import { createTheme } from '@mui/material/styles';

// Dark theme
export const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: '#06b6d4', // cyan-500 - matches your current primary
            light: '#22d3ee', // cyan-400
            dark: '#0891b2'   // cyan-600
        },
        secondary: {
            main: '#ef4444', // red-500 - for accent elements like the watch button
            light: '#f87171', // red-400
            dark: '#dc2626'   // red-600
        },
        background: {
            default: '#0f172a', // slate-950 - main background
            paper: '#1e293b',   // slate-800 - cards, sidebar
            elevated: '#334155' // slate-700 - hover states
        },
        text: {
            primary: '#f1f5f9',   // slate-100 - main text
            secondary: '#94a3b8', // slate-400 - secondary text
            disabled: '#64748b'   // slate-500 - disabled text
        },
        divider: '#334155', // slate-700
        action: {
            hover: 'rgba(6, 182, 212, 0.08)', // primary with low opacity
            selected: 'rgba(6, 182, 212, 0.12)'
        }
    },
    typography: {
        fontFamily: `'Roboto', 'Arial', sans-serif`
    }
});

// Light theme
export const lightTheme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#0891b2', // cyan-600 - slightly darker for better contrast
            light: '#06b6d4', // cyan-500
            dark: '#0e7490'   // cyan-700
        },
        secondary: {
            main: '#dc2626', // red-600 - darker red for better contrast
            light: '#ef4444', // red-500
            dark: '#b91c1c'   // red-700
        },
        background: {
            default: '#ffffff', // white - main background
            paper: '#f8fafc',   // slate-50 - cards, elevated surfaces
            elevated: '#f1f5f9' // slate-100 - hover states
        },
        text: {
            primary: '#0f172a',   // slate-950 - main text
            secondary: '#475569', // slate-600 - secondary text
            disabled: '#94a3b8'   // slate-400 - disabled text
        },
        divider: '#e2e8f0', // slate-200
        action: {
            hover: 'rgba(8, 145, 178, 0.04)', // primary with low opacity
            selected: 'rgba(8, 145, 178, 0.08)'
        }
    },
    typography: {
        fontFamily: `'Roboto', 'Arial', sans-serif`
    }
});

// Default export (you can choose which one to use as default)
export const muiTheme = darkTheme;