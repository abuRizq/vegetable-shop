import type { Config } from 'tailwindcss'

const config: Config = {
  darkMode: 'class',  // Enabling dark mode based on class, you can also use 'media'
  content: [
    './src/**/*.{js,ts,jsx,tsx}',     // ✅ ADD THIS
    './app/**/*.{js,ts,jsx,tsx}',     // ✅ Keep this
    './pages/**/*.{js,ts,jsx,tsx}',   // ✅ If using pages
    './widget/**/*.{js,ts,jsx,tsx}', // ✅ If you have components
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
      },
    },
  },
  plugins: [],
}

export default config