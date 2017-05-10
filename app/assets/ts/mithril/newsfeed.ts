(<any>window).News = {
    controller: function (args: any) {
        document.title = "Toolkit Updates";
        let getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(100);
        return {
            articles: m.request({
                url: getRecentArticlesRoute.url,
                method: getRecentArticlesRoute.method
            })
        };
    },
    view: function (ctrl: any) {
        return [
            m("div", {
                    "class": "large-12 padded-column columns show-for-large",
                    id: "content"
                }, [
                    m("span", {class: "newsfeed-title"}, "Toolkit Updates"),
                    m("div", {
                            "class": "row news_container small-up-1 medium-up-2 large-up-3"
                        },
                        ctrl.articles().map(function (article : any) { // TODO this javascript error existed before migration "Uncaught TypeError: Cannot read property 'map' of undefined"
                            return m("div", {
                                    "class": "column column-block news-tile_main_container"
                                },
                                m("div", {"class": "news-container"},
                                    m("div", {"class": "news-title"},
                                        m("a", article.title),
                                        m("hr")),
                                    m("div", {"class": "news-text"},
                                        m("p", article.textlong),
                                    ),
                                    m("div", {class: "news-image", 'style': {'background-image': 'url(' + article.imagePath + ')'}})
                                )
                            )

                        })
                    )
                    ]
            ),
        ]
    }
};




