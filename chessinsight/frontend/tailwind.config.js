/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        chess: {
          dark: '#0f172a',
          darker: '#0a0f1d',
          card: '#1e293b',
          border: '#334155',
          green: '#10b981',
          greenHover: '#059669',
          gold: '#f59e0b',
          muted: '#94a3b8',
        }
      }
    },
  },
  plugins: [],
}
