import {defineConfig} from 'vitest/config';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
    server: {
        host: true,
        port: 5173,
        proxy: {
            // The backend runs on the host (docs/local-dev.md); inside the container that is host.docker.internal.
            '/api': {
                target: process.env['API_PROXY_TARGET'] ?? 'http://localhost:8080',
                changeOrigin: true,
            },
        },
    },
    plugins: [react()],
    test: {
        environment: 'jsdom',
        setupFiles: ['./vitest.setup.ts'],
        css: {
            modules: {classNameStrategy: 'non-scoped'},
        },
    },
});
