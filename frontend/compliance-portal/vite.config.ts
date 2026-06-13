import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('@azure/msal-browser')) return 'auth'
          if (id.includes('react-router-dom') || id.includes('react-dom') || id.includes('/react/')) return 'react'
          if (id.includes('@tanstack/react-query') || id.includes('axios')) return 'query'
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
