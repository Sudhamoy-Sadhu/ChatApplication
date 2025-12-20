import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],

  server: {
    port: 3000,
  },

  // 🔹 Fix for sockjs-client / stompjs
  define: {
    global: "globalThis",
  },

  resolve: {
    alias: {
      process: "process/browser",
      buffer: "buffer",
    },
  },

  optimizeDeps: {
    include: ["sockjs-client"],
  },
});
