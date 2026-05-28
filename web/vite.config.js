import { defineConfig } from 'vite';

export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:3000',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      input: {
        main: './index.html',
        adminDashboard: './admin/index.html',
        adminProducts: './admin/products.html',
        adminOrders: './admin/orders.html'
      }
    }
  }
});
