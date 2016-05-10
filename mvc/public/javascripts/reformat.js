/* global m: false */
// TODO: ensure this targets the current API.


// Intermediate format: JSON

//model
window.reformat = (function () {
    "use strict"



    function readFastaText(fastaText){


        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1;

        for (; i < splittedStrings.length; i++) {

            result += JSON.stringify(new readFastaLine(splittedStrings[i]));

        }

        return result;
    }


    function readFastaLine(fastaLine) {

        var splittedStrings  = fastaLine.split('\n'),
            result = {},
            i = 1;
        result.name = splittedStrings[0].substr(1, splittedStrings[0].length - 1);
        result.sequence = '';
        for (; i < splittedStrings.length; i++) {
            result.sequence += splittedStrings[i];
        }
        return result;
    }



    function clustalParser() {}


    function writeFasta() {}


    function writeClustal() {}




    return {
        controller: function () {
            this.source = m.prop("")
            this.output = m.prop("")

            this.convert = function () {
                var source = this.source()
                return this.output("Test")
            }.bind(this)
        },

        view: function (ctrl) {
            return m("div", [
                m("textarea", {
                    autofocus: true,
                    style: {width: "100%", height: "250px"},
                    onchange: m.withAttr("value", ctrl.source)
                }, ctrl.source()),
                m("button", {onclick: ctrl.convert}, "Convert"),
                m("textarea", {style: {width: "100%", height: "250px"}},
                    ctrl.output())
            ])
        }
    }
})()