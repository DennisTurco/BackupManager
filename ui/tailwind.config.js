/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/renderer/src/**/*.{ts,tsx,html}'],
  darkMode: ['class', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        /* FlatDarcula-inspired palette */
        bm: {
          0:  '#1e1f22',
          1:  '#2b2d30',
          2:  '#313335',
          3:  '#3c3f41',
          4:  '#45494a',
          border: '#515254',
          'border-light': '#6b6d6e',
          accent: '#2196f3',
          'accent-dim': '#1565c0',
          text: '#dfe1e5',
          muted: '#8c8c8c',
          dim: '#6b6d6e',
          success: '#5aad4e',
          error:   '#e05252',
          warning: '#e8a735',
        },
        /* Light palette overrides */
        light: {
          0: '#f5f5f5',
          1: '#ffffff',
          2: '#ebebeb',
          3: '#e0e0e0',
          border: '#c9c9c9',
          text: '#1a1a1a',
          muted: '#6b6b6b',
        }
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Consolas', 'Menlo', 'monospace'],
      }
    }
  },
  plugins: []
}
