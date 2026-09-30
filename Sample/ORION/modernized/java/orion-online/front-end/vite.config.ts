import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Dev proxy: forward /api and /manifest to the Spring backend.
export default defineConfig({
  plugins: [vue()],
  // Alias '@' = src/ — HỢP ĐỒNG của UI kit dùng chung (frontend/kit dùng '@/components/…').
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
