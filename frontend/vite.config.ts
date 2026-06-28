import { defineConfig } from 'vite';
import { viteStaticCopy } from 'vite-plugin-static-copy';

export default defineConfig({
  plugins: [
    viteStaticCopy({
      targets: [
        { src: 'public/models/*', dest: 'models' }
      ]
    })
  ],
  assetsInclude: ['**/*.bin', '**/*-shard*', '**/*-shard1', '**/*-shard2'],
  optimizeDeps: {
    exclude: ['face-api.js']
  }
});