// tailwind.config.ts
import type { Config } from 'tailwindcss'

export default {
    darkMode: 'class',  // Enabling dark mode based on class, you can also use 'media'
    content: [
        './src/**/*.{js,ts,jsx,tsx}',     // ✅ ADD THIS
        './app/**/*.{js,ts,jsx,tsx}',     // ✅ Keep this
        './pages/**/*.{js,ts,jsx,tsx}',   // ✅ If using pages
        './components/**/*.{js,ts,jsx,tsx}', // ✅ If you have components
    ],
    theme: {
        extend: {
            colors: {
                // Primary colors for both themes
                primary: {
                    DEFAULT: 'hsl(var(--primary))',  // Cyan for both light and dark
                    light: '#22d3ee',   // Cyan-400
                    dark: '#0891b2',    // Cyan-600
                },
                secondary: {
                    DEFAULT: '#ef4444', // Red-500 for both light and dark
                    light: '#f87171',   // Red-400
                    dark: '#dc2626',    // Red-600
                },
                background: {
                    default: 'hsl(var(--background))', // Background for both themes
                    paper: 'hsl(var(--paper))',        // Card background for both themes
                    elevated: 'hsl(var(--elevated))',   // Elevated surfaces
                },
                text: {
                    primary: 'hsl(var(--text-primary))',   // Main text color
                    secondary: 'hsl(var(--text-secondary))', // Secondary text
                    disabled: 'hsl(var(--text-disabled))', // Disabled text color
                },
                divider: 'hsl(var(--divider))', // Divider color
                action: {
                    hover: 'hsl(var(--action-hover))', // Hover state action
                    selected: 'hsl(var(--action-selected))', // Selected state action
                },
            },
            // Use font family as defined in MUI theme
            fontFamily: {
                sans: ['Roboto', 'Arial', 'sans-serif'],
            },
        },
    },
    plugins: [],
} satisfies Config