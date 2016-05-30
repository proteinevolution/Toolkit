



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


    function fastaToClustalLine (fastaLine) {

        var line = readFastaLine(fastaLine);
        


    }





    function printAsJSON(source) {


        var output = JSON.stringify(readFastaText(source));

        return output;
    }




    function writeFasta() {}


    function writeClustal() {

        var output = "";
        return output;
    }






