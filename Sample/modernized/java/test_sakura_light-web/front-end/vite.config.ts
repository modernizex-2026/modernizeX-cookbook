import { fileURLToPath, URL } from 'node:url';
import vue from '@vitejs/plugin-vue';
import { defineConfig, loadEnv } from 'vite';

const srcDir = fileURLToPath(new URL('./src', import.meta.url));

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  // BE origin for dev-mode proxying; defaults to the local runner.
  const beTarget = env.BE_PROXY_TARGET?.trim() || 'http://localhost:8080';

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': srcDir,
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/ws': { target: beTarget, ws: true, changeOrigin: true },
        '/api': { target: beTarget, changeOrigin: true },
      },
    },
  };
});
