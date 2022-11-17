import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue2';
import viteImagemin from 'vite-plugin-imagemin';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    // https://github.com/vitejs/vite/issues/1930
    const env = loadEnv(mode, process.cwd());
    return {
        plugins: [
            vue({
                template: {
                    compilerOptions: {
                        compatConfig: {
                            MODE: 2,
                        },
                    },
                },
            }),
            viteImagemin({
                bypassOnDebug: true,
                mozjpeg: {
                    progressive: true,
                    quality: 95,
                },
                // optipng.enabled: false will disable optipng
                optipng: {
                    enabled: true,
                },
                pngquant: {
                    quality: [0.7, 0.9],
                    speed: 4,
                },
                gifsicle: {
                    interlaced: true,
                },
                svgo: {},
                // TODO use webp version of images after update to Vue 3 (and dropping IE)
                // webp: {
                //     quality: 75
                // }
            }),
        ],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, './src'),
                vue: '@vue/compat',
            },
        },
        css: {
            preprocessorOptions: {
                scss: {
                    additionalData: `
          @import "@/assets/scss/_variables.scss";
        `,
                },
            },
        },
        server: {
            host: true, // this should also expose it in the network to work on olt
            port: 8080,
            proxy: {
                '^/api': env?.VITE_BACKEND_URL ?? 'http://localhost:9000',
                '/ws/': {
                    target: env?.VITE_WS_URL ?? 'ws://localhost:9000',
                    secure: false,
                    ws: true,
                },
            },
        },
        build: {
            commonjsOptions: {
                /**
                 * Setting to make prod-build working with vue-slider-component
                 * https://github.com/NightCatSama/vue-slider-component/issues/642
                 **/
                requireReturnsDefault: true,
            },
        },
    };
});
