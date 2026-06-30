import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  define: {
    // sockjs-client expects a global object in the browser bundle
    global: 'globalThis',
  },
  server: {
    port: 5173,
  },
});
