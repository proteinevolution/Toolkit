module.exports = {
    chainWebpack: config => {

        // use image-webpack-loader for images
        const imageRule = config.module.rule('images');
        imageRule.use('image-webpack-loader')
            .loader('image-webpack-loader')
            .tap(args => {
                return {
                    bypassOnDebug: true,
                    mozjpeg: {
                        progressive: true,
                        quality: 10
                    },
                    // optipng.enabled: false will disable optipng
                    optipng: {
                        enabled: true,
                    },
                    pngquant: {
                        quality: '65-90',
                        speed: 4
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

        // use sass-resources-loader to supply global variables
        const scssRule = config.module.rule('scss');
        scssRule.oneOf('vue').use('sass-resources-loader')
            .loader('sass-resources-loader')
            .tap(args => {
                return {
                    resources: [
                        './src/assets/scss/_variables.scss',
                    ],
                };
            });
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
            }
        },
    },
};
