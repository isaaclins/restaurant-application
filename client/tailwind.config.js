/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // KDS Status Colors
        'status-new': '#ef4444',      // Red - New Order
        'status-prep': '#f59e0b',     // Yellow/Amber - Preparing
        'status-ready': '#22c55e',    // Green - Ready
        'status-completed': '#6b7280', // Gray - Completed
      },
    },
  },
  plugins: [],
}
