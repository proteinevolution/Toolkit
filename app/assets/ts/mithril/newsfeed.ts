(<any>window).News = {

    controller: function(args : any) {
        document.title = "Toolkit Updates";
    },
    view: function(ctrl : any) {
        return [
            m("div", {
                    "class": "large-2 padded-column columns show-for-large",
                    id: "sidebar",
                    //config: fadesIn
                }, [
                    // no content in sidebar for newsfeed
                ]
            ),
            m("div", {
                    "class": "large-10 padded-column columns show-for-large",
                    id: "content"}, [
                    m("h3", "Toolkit Updates")
                ]
            )
        ]
    }

};