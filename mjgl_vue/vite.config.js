import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_DEV_PROXY_TARGET || 'http://127.0.0.1:8080'

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    define: {
      // 兼容 sockjs-client 在浏览器环境中使用 Node 全局变量 global
      global: 'window',
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
        },
        // WebSocket 握手与后续帧都代理到后端
        '/ws': {
          target: proxyTarget,
          ws: true,
        },
      },
    },
  }
})
