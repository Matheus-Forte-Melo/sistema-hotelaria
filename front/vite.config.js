import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'BACKEND_')
  return {
    plugins: [react(), tailwindcss()],
    server: { proxy: { '/api': env.BACKEND_URL || 'http://127.0.0.1:8080' } },
  }
})
