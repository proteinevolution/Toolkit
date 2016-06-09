/*

REFORMAT

 */



    function readFastaText(fastaText){


        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1;

        for (; i < splittedStrings.length; i++) {

            result += JSON.stringify(new readFastaLine(splittedStrings[i]));

        }

        return result;
    }


    function printClustalText(fastaText){


        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1,
            j = 0;


        for (; j<Math.trunc(getClustalSeq(splittedStrings[i]).length/60)+1 ; j++){

        for (; i < splittedStrings.length; i++) {

                result += getClustalHeader(splittedStrings[i]);
                result += "\t";
                result += chunkString(getClustalSeq(splittedStrings[i]), 60)[j];
                result += "\n";

        }
            result += "\n\n";
            i = 1;

        }

        return result;
    }


    function readFastaLine(fastaLine) {

        var splittedStrings  = fastaLine.split('\n'),
            result = {},
            i = 1;
        result.name = splittedStrings[0].substr(0, 11);
        result.sequence = '';
        for (; i < splittedStrings.length; i++) {
            result.sequence += splittedStrings[i];
        }
        return result;
    }


    function getClustalSeq (fastaLine) {

        var fasta = readFastaLine(fastaLine),
            result = {};


        return fasta.sequence;

    }

    function getClustalHeader (fastaLine) {

        var fasta = readFastaLine(fastaLine);
        return fasta.name;


    }

    function chunkString(str, len) {
        var _size = Math.ceil(str.length/len),
            _ret  = new Array(_size),
            _offset
            ;

        for (var _i=0; _i<_size; _i++) {
            _offset = _i * len;
            _ret[_i] = str.substring(_offset, _offset + len);
        }

        return _ret;
    }



    function chunkSubstr(str, size) {
        var numChunks = Math.ceil(str.length / size),
            chunks = new Array(numChunks);

        for(var i = 0, o = 0; i < numChunks; ++i, o += size) {
            chunks[i] = str.substr(o, size);
        }

        return chunks;
    }



    function printAsJSON(source) {


        var output = JSON.stringify(readFastaText(source));

        return output;
    }






