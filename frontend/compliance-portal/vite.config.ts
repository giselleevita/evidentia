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
      '/api/v1/evidence': { target: 'http://localhost:8080', changeOrigin: true },
      '/api/v1/auth': { target: 'http://localhost:8080', changeOrigin: true },
      '/api/v1/audit': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/v1/ratings': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/v1/incidents': { target: 'http://localhost:8083', changeOrigin: true },
      '/api/v1/compliance': { target: 'http://localhost:8084', changeOrigin: true },
      '/api/v1/reports': { target: 'http://localhost:8085', changeOrigin: true },
      '/api/v1/collectors': { target: 'http://localhost:8086', changeOrigin: true },
      '/api/v1/integrations': { target: 'http://localhost:8086', changeOrigin: true },
      '/api/v1/billing': { target: 'http://localhost:8087', changeOrigin: true },
      '/api/v1/features': { target: 'http://localhost:8087', changeOrigin: true },
    },
  },
})
