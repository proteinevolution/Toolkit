/* eslint-disable @typescript-eslint/no-var-requires */
const webpack = require('webpack');

module.exports = {
    chainWebpack: config => {
        // we don't want to prefetch languages
        config.plugins.delete('preload');
        config.plugins.delete('prefetch');
        // config.plugin('prefetch').tap(options => {
        //     options[0].fileBlacklist = options[0].fileBlacklist || [];
        //     options[0].fileBlacklist.push(/lang(.)+?\.js$/);
        //     return options;
        // });

        // use image-webpack-loader for images
        const imageRule = config.module.rule('images');
        imageRule.use('image-webpack-loader')
            .loader('image-webpack-loader')
            .tap(() => {
                return {
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
                    // the webp option will enable WEBP, this breaks at the moment
                    // webp: {
                    //     quality: 75
                    // }
                };
            });
    },
    css: {
        loaderOptions: {
            sass: {
                additionalData: `
          @import "@/assets/scss/_variables.scss";
        `,
            },
        },
    },
    devServer: {
        proxy: {
            '^/api': {
                target: 'http://localhost:' + process.env.VUE_APP_BACKEND_PORT || '9000',
                pathRewrite: {
                    '^/api/': '/api/', // use this to later remove base path
                },
            },
            '/ws/': {
                target: 'ws://localhost:' + process.env.VUE_APP_BACKEND_PORT || '9000',
                secure: false,
                ws: true,
            },
        },
    },
    configureWebpack: {
        plugins: [
            // add languages here
            new webpack.ContextReplacementPlugin(/moment[/\\]locale$/, /de/),
        ],
    },
};
