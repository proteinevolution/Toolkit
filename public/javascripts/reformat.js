/*

REFORMAT ES6 VERSION
TODO: Minify me

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


    function readA3mText(a3mtext){


        var splittedStrings = a3mtext.split(">"),
            result = [],
            i = 1;

        for (; i < splittedStrings.length; i++) {

            result += ">";
            result += readA3mLine(splittedStrings[i]).name;
            result += "\n";
            result += readA3mLine(splittedStrings[i]).sequence;
            result += "\n";

        }

        return result;

    }


    function readA3mLine(a3mline){

        var splittedStrings  = a3mline.split('\n'),
            result = {},
            i = 1;
        result.name = splittedStrings[0].substr(0, 11);
        result.sequence = '';
        for (; i < splittedStrings.length; i++) {
            result.sequence += splittedStrings[i];
            result.sequence = result.sequence.split('.').join('');
        }
        return result;

    }


    function printClustalText(fastaText){


        // clean header from multiple occurences of > identifiers, will be missing in output for now.
        var newlines = fastaText.split('\n');
        for(var k = 0;k < newlines.length;k++){
            if ((newlines[k].match(/>/g)||[]).length > 1) {
                newlines[k] = newlines[k].replace(/(?!^)>/g, '');
            }
        }

        fastaText = newlines.join('\n');

        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1,
            j = 0;

            result += "CLUSTAL multiple sequence alignment";
            result += "\n\n";

        for (; j < Math.trunc(getClustalSeq(splittedStrings[i]).length/60) + 1 ; j++){

            for (; i < splittedStrings.length; i++) {

                    result += getClustalHeader(splittedStrings[i]);
                    result += "\t";
                    result += chunkString(getClustalSeq(splittedStrings[i]), 60)[j];
                    result += "\n";

            }

            result += "\n\n";
            i = 1;

        }

        result = result.slice(0, -3); //removes trailing whitespaces at EOF

        return result;
    }


    function getGIs(fastaText){

        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1;

            for (; i < splittedStrings.length; i++) {

                if (splittedStrings[i].substring(0,3) == 'gi|') {
                result += getClustalHeader(splittedStrings[i]).substring(3);
                result += "\n";

                }
            }

        return result;
    }


    function readFastaLine(fastaLine) {

        var splittedStrings  = fastaLine.split('\n'),
            result = {},
            i = 1;
        result.name = splittedStrings[0].substr(0, 12);
        result.sequence = '';
        for (; i < splittedStrings.length; i++) {
            result.sequence += splittedStrings[i];
        }
        return result;
    }


    function getClustalSeq (fastaLine) {

        var fasta = readFastaLine(fastaLine);
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


    function printAsJSON(source) {

        return JSON.stringify(readFastaText(source));

    }

    function clustalAsJSON(source){

        return JSON.stringify(clustalParser(source));

    }


    function clustal2Fasta(text) {

        var clustalObj = clustalParser(text),
            result = [];


        for(var i=0;i<clustalObj.length;i++){
            result +=">";
            result += clustalObj[i].name;
            result += "\n";
            result += clustalObj[i].seq;
            result += "\n";
        }

        return result;

    }


    function _contains(text, search) {
        return ''.indexOf.call(text, search, 0) !== -1;
    }


    function clustalParser(text) {
        var blockstate, cSeq, k, keys, label, line, lines, match, obj, regex, seqCounter, seqs, sequence;
        seqs = [];
        if (Object.prototype.toString.call(text) === '[object Array]') {
            lines = text;
        } else {
            lines = text.split("\n");
        }
        if (lines[0].slice(0, 6) === !"CLUSTAL") {
            throw new Error("Invalid CLUSTAL Header");
        }
        k = 0;
        blockstate = 1;
        seqCounter = 0;
        while (k < lines.length) {
            k++;
            line = lines[k];
            if ((line == null) || line.length === 0) {
                blockstate = 1;
                continue;
            }
            if (line.trim().length === 0) {
                blockstate = 1;
                continue;
            } else {
                if (_contains(line, "*")) {
                    continue;
                }
                if (blockstate === 1) {
                    seqCounter = 0;
                    blockstate = 0;
                }
                regex = /^(?:\s*)(\S+)(?:\s+)(\S+)(?:\s*)(\d*)(?:\s*|$)/g;
                match = regex.exec(line);
                if (match != null) {
                    label = match[1];
                    sequence = match[2];
                    if (seqCounter >= seqs.length) {
                        obj = getMeta(label);
                        label = obj.name;
                        cSeq = new model(sequence, label, seqCounter);
                        cSeq.ids = obj.ids || {};
                        cSeq.details = obj.details || {};
                        keys = Object.keys(cSeq.ids);
                        if (keys.length > 0) {
                            cSeq.id = cSeq.ids[keys[0]];
                        }
                        seqs.push(cSeq);
                    } else {
                        seqs[seqCounter].seq += sequence;
                    }
                    seqCounter++;
                } else {
                    console.log("clustal parse error, maybe fasta?", line);
                }
            }
        }

        return seqs;
    }


    function getMeta(label) {

            var full_id = false, full_desc = false;
            var name, ids = {}, details = {}, description;

        // 	console.log( "getMeta.label: ", label );

            var label_parts = label.split(" ");

            if ( label_parts.length >= 1 ) {
                full_id   = label_parts.shift();     // everything up to the first white space
                full_desc = label_parts.join(" ");   // everything else
            }
            else {
                full_id = label;
            }

        // 	console.log( "full_id", full_id );
        // 	console.log( "full_desc", full_desc );

            if ( full_id ) {
                var id_parts = full_id.split('|');

                // the last item is the accession
                name = id_parts.pop();

                details.en = name;

                // everything else should be pairs: db|id
                while ( id_parts.length != 0 ) {
                    var db = id_parts.shift();
                    var id = id_parts.shift();
                    ids[ db ] = id;
                }
            }
            else {
                name = full_id;
            }

            if ( full_desc ) {

                var kv_parts = full_desc.split('=');

                if ( kv_parts.length > 1 ) {

                    var current_key, next_key;
                    var kv;
                    var kv_idx_max = kv_parts.length - 1;
                    var kv_idx = 0;
                    kv_parts.forEach( function( value_and_maybe_next_key ) {

                        value_and_maybe_next_key = value_and_maybe_next_key.trim();

                        var value_parts = value_and_maybe_next_key.split(" ");
                        var value;
                        if ( value_parts.length > 1 ) {
                            next_key = value_parts.pop();
                            value = value_parts.join(' ');
                        }
                        else {
                            value = value_and_maybe_next_key;
                        }

                        if ( current_key ) {
                            var key = current_key.toLowerCase();
                            details[ key ] = value;
                            //console.log( "details[" + key + "] = " + value );
                        }
                        else {
                            description = value;
                            //console.log( "description=" + value );
                        }
                        current_key = next_key;
                    });
                }
                else {
                    description = kv_parts.shift();
                }
            }

            var meta = {
                name: name,
                ids: ids,
                details: details
            };

            if ( description ) {
                meta.desc = description
            }

        // 	console.log( "meta", meta );

            return meta;
        }


    function model(seq, name, id) {

        this.seq = seq;
        this.name = name;
        this.id = id;
        this.ids = {};

    }

    function validateFasta(fasta) {

        if (!fasta) {
            return false;
        }


        // checks double occurrences of ">" in the header

        var newlines = fasta.split('\n');
        for(var k = 0;k < newlines.length;k++){
            if ((newlines[k].match(/>/g)||[]).length > 1) {
                console.log("warning, header has more than one > identifier. file corrupt?");
                //newlines[k] = newlines[k].replace(/(?!^)>/g, '');
            }
        }


        //fasta = newlines.join('\n');

        if (!fasta.startsWith('>')) { return false; }
        if (fasta.indexOf('>') == -1) { return false; }

        var splittedStrings = fasta.split(">"),
            i = 1;
        //console.log(splittedStrings);
        for (; i < splittedStrings.length; i++) {

            //reinsert seperator
            var seq = ">" + splittedStrings[i];

            // immediately remove trailing spaces
            seq = seq.trim();

            // split on newlines...
            var lines = seq.split('\n');

            // check for header
            if (seq[0] == '>') {
                // remove one line, starting at the first position
                lines.splice(0, 1);

            }

            // join the array back into a single string without newlines and
            // trailing or leading spaces
            seq = lines.join('').trim();

            if (seq.search(/[^\-\\.ACDEFGHIKLMNPQRSTUVWXY\s]/i) != -1) {

                return false;
            }

        }

        return true;
    }



  function checkClustal (clustal) {

      if(!clustal) { return false; }

        var header, headerSeen, i, len, lines, sequence;
        clustal = clustal.split('\n');
        headerSeen = false;
        //check if it's an alignment:
        var clustalObj = (clustalParser(clustal));

        for (var j = 1; j < clustalObj.length; j++) {
            if (clustalObj[j].seq.length !== clustalObj[j-1].seq.length) {
                console.log('input is not an alignment');
                return false; }
        }


        for (i = 0, len = clustal.length; i < len; i++) {
            sequence = clustal[i];

            if (sequence.match(/^\s*$/)) {
                continue;
            }

            if (headerSeen === true) {
                sequence = sequence.trim();
                lines = sequence.split(/\s+/g);

                if (lines.length !== 2 && lines.length !== 3) {

                    console.log("Each line has to include name/sequence and optional length");
                    return false;
                }
                if (lines[1].length > 60) {

                    console.log('More than 60 sequence symbols in one line');
                    return false;
                }

                if (lines[1].search(/[^\-\\.ACDEFGHIKLMNPQRSTUVWXY\s]/i) != -1) {

                    return false;
                }

            } else {
                header = sequence.trim().replace(' ', '');
                if (!header.startsWith('CLUSTAL')) {

                    console.log('No CLUSTAL Header');
                    return false;
                }
                headerSeen = true;
            }
        }
      return true;
    }

    function validateA3m(a3m) {
        if (a3m.indexOf('.') != -1) { return false; }
        validateFasta(a3m);
    }


    function validateA2m(a2m) {

        if ((a2m.indexOf('.') != -1) && (validateFasta(a2m)))
            return true;
    }