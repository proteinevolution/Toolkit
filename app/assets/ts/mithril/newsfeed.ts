// Velocity animation config

let fadesIn = function(element : any, isInitialized : boolean, context : any) {

    let url = window.location.href;
    let parts = url.split("/");
    let isJob = parts[parts.length-2] == "jobs";

    if (!isInitialized && !isJob) {
        element.style.opacity = 0;
        $(element).velocity({opacity: 1, top: "50%"}, 750);
    }
};


(<any>window).News = {

    controller: function(args : any) {
        document.title = "Toolkit Updates";
    },
    view: function(ctrl : any) {
        return [
            m("div", {
                    "class": "large-2 padded-column columns show-for-large",
                    id: "sidebar",
                    config: fadesIn
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