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

        // use vue-svg-loader for svg
        const svgRule = config.module.rule('svg');
        svgRule.uses.clear();
        svgRule
            .use('vue-svg-loader')
            .loader('vue-svg-loader');
    },
};
