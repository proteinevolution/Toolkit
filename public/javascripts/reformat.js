/*

 REFORMAT ES6 VERSION
 TODO: Minify me

 */

function readFastaText(fastaText){

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
        i = 1;

    for (; i < splittedStrings.length; i++) {

        result.push(new readFastaLine(splittedStrings[i]));

    }


    return result;
}


function printFastaObj(obj) {

    result = [];

    for(var i=0;i<obj.length;i++){
        result +=">";
        result += obj[i].name;
        result += "\n";
        result += obj[i].seq;
        result += "\n";
    }

    return result;
}


function readA3mText(a3mtext){


    // clean header from multiple occurences of > identifiers, will be missing in output for now.
    var newlines = a3mtext.split('\n');
    for(var k = 0;k < newlines.length;k++){
        if ((newlines[k].match(/>/g)||[]).length > 1) {
            newlines[k] = newlines[k].replace(/(?!^)>/g, '');
        }
    }

    a3mtext = newlines.join('\n');



    var splittedStrings = a3mtext.split(">"),
        result = [],
        i = 1;

    for (; i < splittedStrings.length; i++) {

        result += ">";
        console.log(splittedStrings[0]);
        result += readA3mLine(splittedStrings[i]).name;
        result += "\n";
        result += readA3mLine(splittedStrings[i]).seq;
        result += "\n";

    }

    return result;

}


function readA3mLine(a3mline){

    var splittedStrings,
        result,
        i = 1;

    result = {};
    splittedStrings = a3mline.split('\n')
    result.name = splittedStrings[0];

    result.seq = '';
    for (; i < splittedStrings.length; i++) {
        result.seq += splittedStrings[i];
    }
    return result;

}


function printClustalText(fastaText){


    // clean header from multiple occurences of > identifiers, will be missing in output for now.
    var newlines = fastaText.split('\n');
    //console.log(newlines);
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
    result += "\n"; // hack for codemirror cursor bug with atomic ranges


    return result;
}


function getGIs(fastaText){

    var splittedStrings = fastaText.split(">"),
        result = [],
        i = 1;

    for (; i < splittedStrings.length; i++) {

        if (splittedStrings[i].substring(0,3) == 'gi|') {
            result += getClustalHeader(splittedStrings[i]).substring(3).split('|')[0];
            result += "\n";

        }
    }

    return result;
}



function getAccessionversion(json){
    var result= '';
    for (var i= 0; i < json.length; i++) {
        var split = json[i].name.split(/\s/g);
        if(!split[0].toUpperCase().match("GI")) {
            result += split[0];
            result += "\n";
        }
    }

    return result;
}

function readFastaLine(fastaLine) {

    var splittedStrings  = fastaLine.split('\n'),
        result = {},
        i = 1;

    //if (splittedStrings[0].charAt(12) === '|'){
    result.name = splittedStrings[0].substr(0, 28);
    //else { result.name = splittedStrings[0].substr(0, 11) + ' '; }

    result.seq = '';
    for (; i < splittedStrings.length; i++) {
        result.seq += splittedStrings[i];
    }
    return result;
}


function getClustalSeq (fastaLine) {

    var fasta = readFastaLine(fastaLine);
    return fasta.seq;

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
        result += clustalObj[i].untrimmed;
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
    var blockstate, cSeq, k, keys, untrimmed, label, line, lines, match, obj, regex, seqCounter, seqs, sequence;
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
                untrimmed = label;
                sequence = match[2];
                if (seqCounter >= seqs.length) {
                    obj = getMeta(label);
                    label = obj.name;
                    cSeq = new model(sequence, label, seqCounter);
                    cSeq.untrimmed = untrimmed;
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
                //console.log("clustal parse error, maybe fasta?", line);
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
    if (!newlines[0].startsWith("#") && newlines[0].startsWith(">")) {
        /*
         for(var k = 0;k < newlines.length;k++){
         if ((newlines[k].match(/>/g)||[]).length > 1) {
         throw new Error("warning, header has more than one > identifier. file corrupt?");
         return false;
         }

         }
         */
        //fasta = newlines.join('\n');

        if (!fasta.startsWith('>')) {
            return false;
        }
        if (fasta.indexOf('>') == -1) {
            return false;
        }




        var splittedStrings = fasta.split(">"),
            i = 1;



        //console.log(splittedStrings);
        for (; i < splittedStrings.length; i++) {

            // only pir format has a ';' as the 4. char
            if(splittedStrings[i].charAt(4) == ';')
                return false;
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

            if (/[^\-\\ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(seq)) {
                return false;
            }

        }

        return true;

    }
    return false;
}



function validateClustal (clustal) {

    if(!clustal) { return false; }

    var header, headerSeen, i, len, lines, sequence;
    clustal = clustal.split('\n');
    headerSeen = false;
    //check if it's an alignment:
    var clustalObj = (clustalParser(clustal));


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







        } else {
            header = sequence.trim().replace(' ', '');
            if (!header.startsWith('CLUSTAL')) {

                console.log('No CLUSTAL Header');
                return false;
            }else{
                for (var j = 1; j < clustalObj.length; j++) {
                    if (clustalObj[j].seq.length !== clustalObj[j-1].seq.length) {
                        console.log('input is not an alignment');
                        return false; }

                    if (/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(clustalObj[j].seq)) {
                        throw new Error("Alignment contains invalid symbols.");
                        return false;
                    }
                }

            }
            headerSeen = true;
        }
    }
    return true;
}


function validateA2m(a2m) {

    if ((a2m.indexOf('.') != -1) && (validateFasta(a2m)))
        return true;
}

function validateAlignmentFasta(aln) {
    if(!aln)
        return false;
    // check whether a fasta input is an alignment. This will be useful for validating whether we can directly
    // convert from fasta to clustal. if not -> suggest forwarding to Muscle.
    if(aln.startsWith(">")) {
        var fastaObj = readFastaText(aln);
        var firstlength = fastaObj[0].seq.length;


        for (var i = 0; i < fastaObj.length; i++) {

            if (fastaObj[i].seq.length !== firstlength) {
                console.log("input is not an alignment");
                if (_contains(fastaObj[i].seq, "-")) {

                    console.log("warning: input contains dashes without being an alignment")
                }
                return false;
            }

        }

        console.log("this is an alignment");
        return true;
    }
    return false;
}


function aminoCount(seq) {

    // returns array that contains char count of sequence at position result[sequence][Unicode(char)]
    var uni;

    uni = new Uint32Array(91);
    for (var i = 0; i < uni.byteLength; i++) {
        uni[seq.charCodeAt(i)] += 1;
    }
    return uni;
}

/* transform fasta sequences to lowercase and returns parsed json object */

function fastaToLowerCase(fas) {

    var fastaObj = readFastaText(fas);
    for (var i = 0; i<fastaObj.length; i++) {
        fastaObj[i].seq = fastaObj[i].seq.toLowerCase();
    }
    return fastaObj;

}


/* transform fasta sequences to uppercase and returns parsed json object */

function fastaToUpperCase(fas) {

    var fastaObj = readFastaText(fas);
    for (var i = 0; i<fastaObj.length; i++) {
        fastaObj[i].seq = fastaObj[i].seq.toUpperCase();
    }
    return fastaObj;

}

/* transform clustal sequences to lowercase and returns parsed json object */

function clustalToLowerCase(clu) {

    var clustalObj = clustalParser(clu);
    for (var i = 0; i<clustalObj.length; i++) {
        clustalObj[i].seq = clustalObj[i].seq.toLowerCase();
    }
    return clustalObj;
}


/* transform clustal sequences to uppercase and returns parsed json object */

function clustalToUpperCase(clu) {

    var clustalObj = clustalParser(clu);
    for (var i = 0; i<clustalObj.length; i++) {
        clustalObj[i].seq = clustalObj[i].seq.toUpperCase();
    }
    return clustalObj;
}


function conservation(aln) {

    //TODO: compute the conservation for an alignment

}


function getNumberOfFastaSeqs(fas) {
    var fastaObj = readFastaText(fas);
    return fastaObj.length;
}


function getNumberOfClustalSeqs(clustal) {

    var clustalObj = clustalParser(clustal);
    return clustalObj.length;

}

function validateClustalUpperCase(clu){

    var clustalObj = clustalParser(clu);
    for (var i = 0; i<clustalObj.length; i++) {
        if((/[a-z]/.test(clustalObj[i].seq)))
            return false;
    }
    return true;

}

function validateClustalLowerCase(clu){

    var clustalObj = clustalParser(clu);
    for (var i = 0; i<clustalObj.length; i++) {
        if((/[A-Z]/.test(clustalObj[i].seq)))
            return false;
    }
    return true;
}

function validateFastaUpperCase(fas){

    var fastaObj = readFastaText(fas);
    for (var i = 0; i<fastaObj.length; i++) {
        if((/[a-z]/.test(fastaObj[i].seq)))
            return false;
    }
    return true;
}

function validateFastaLowerCase(fas){

    var fastaObj = readFastaText(fas);
    for (var i = 0; i<fastaObj.length; i++) {
        if((/[A-Z]/.test(fastaObj[i].seq)))
            return false;
    }
    return true;

}


function addSpaceEveryNChars(str, n) {
    var ret = '', i, len;
    for(i = 0, len = str.length; i < len; i += n) {
        ret += (str.substr(i, n));
        ret += " ";
    }
    return ret
}

function validateAlignment(json) {
    if (!json) {
        return false;
    }
    // check whether jason object is an valid alignment.
    if(json.length > 1 ) {
        var firstlength = json[0].seq.length;
        for (var i = 0; i < json.length; i++) {
            if (json[i].seq.length !== firstlength) {
                throw new Error("input is not an alignment");
                if (_contains(json[i].seq, "-")) {
                    throw new Error("warning: input contains dashes without being an alignment");
                }
                return false;

            }
        }
        console.log("this is an alignment");

    }
    return true;

}


function json2fasta(json) {
    var result = '';
    for (var i = 0; i < json.length; i++) {
        result += ">";
        result += json[i].name;
        result += "\n";
        result += formatLongSeq(json[i].seq,60);
        result += "\n";
    }

    return result;
}


function fasta2json(fasta) {

    var newlines  = fasta.split('\n'),
        //remove empty lines
        newlines = newlines.filter(Boolean);

    var result = [], element;

    for(var i = 0; i < newlines.length;){
        element = {};
        element.name = '';
        if(newlines[i].startsWith('>')) {
            element.name = newlines[i].substring(1);

        }
        i++;
        element.seq = '';
        while(i < newlines.length && !newlines[i].startsWith('>')) {
            if(!newlines[i].startsWith(';'))
                element.seq += newlines[i];
            i++;
        }
        result.push(element);
    }

    return result;

}


function validatePhylip(phylip){
    if(!phylip)
        return false;

    var newlines, header, n, m, IDs = [], json = [], element;

    newlines = phylip.split('\n');
    newlines = newlines.filter(Boolean);

    header = newlines[0].match(/\S+/g);
    // check if first char is whitespace (only phylip begins with whitespace)
    if(newlines[0].startsWith(" ") || newlines[0].startsWith("\t")) {

        n = header[0];
        m = header[1];
        if (header.length < 2 || n < 1 || m < 1 || parseInt(n) <= 0 || parseInt(m) <= 0) {
            throw new Error("Incorrect header.");
            return false;
        }

        // delete first lines (it does not contain sequences)
        newlines.shift();

        //checks number of sequences is correct
        if(newlines.length % n != 0)
            return false;

        // parse sequences
        var seq = [];
        for (var i = 0 ;i < n; i++) {
            //delete whitespaces
            newlines[i] = newlines[i].replace(/\s/g, "");
            seq[i] = newlines[i].substring(10);
        }


        //parse rest of the sequence that is separated through breaklines
        for (var i = n ,j = 0; i < newlines.length && newlines.length > n; i++, j++) {

            if(i % n == 0 )
                j = 0;
            //delete whitespaces
            newlines[i] = newlines[i].replace(/\s/g, "");
            seq[j] += newlines[i];
        }

        // check for wrong symbols
        for (var i = 0; i < n; i++) {
            if (/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(seq[i])) {
                throw new Error("Alignment contains invalid symbols.");
                return false;
            }
            // check for number of symbols

            else if (seq[i].length != m) {
                throw new Error("Number of sequence does not match with the header.");
                return false;
            }


        }

        return true
    }
    return false;
}



function json2phylip(json) {

    if(!json)
        return false;
    var result = '', n, m, split;
    n = json.length;
    m = json[0].seq.length;
    result += "\t";
    result += n ;
    result += "\t";
    result += m;

    // extract name and sequence
    for (var i = 0; i < n; i++) {
        result += "\n";
        // if header of fasta is shorter than 10, add whitespace
        result += json[i].name.substring(0, 10);
        result += " ";
        if(json[i].name.substring(0, 10).length < 10) {
            for (var j = 0; j < 10 - json[i].name.substring(0, 9).length;j++) {
                result += " ";
            }
        }

        split = json[i].seq.match(/.{1,60}/g);
        result += addSpaceEveryNChars(split[0],10);

    }
    for (var j = 1; j < split.length; j++) {
        result += '\n\n';
        for (var i = 0; i < n; i++) {
            split = json[i].seq.match(/.{1,60}/g);
            result += '           ';
            result += addSpaceEveryNChars(split[j],10);
            result += '\n';
        }
    }
    result += '\n'

    result += "\n"; // hack for codemirror cursor bug with atomic ranges
    return result;
}




function phylip2json(phylip) {
    if(!phylip)
        return false;
    var newlines, header, n, result = [], element = {};

    newlines = phylip.split('\n');
    //remove empty lines
    newlines = newlines.filter(Boolean);

    header = newlines[0].match(/\S+/g);
    n = header[0];

    // delete first lines (it does not contain sequences)
    newlines.shift();

    // parse sequences
    for (var i = 0 ;i < n; i++) {
        var element = {};
        //delete whitespaces
        newlines[i] = newlines[i].replace(/\s/g, "");
        element.name = newlines[i].substring(0,9);
        element.seq = newlines[i].substring(10);
        result.push(element);
    }


    //parse rest of the sequence that is separated through breaklines
    for (var i = n ,j = 0; i < newlines.length; i++, j++) {

        if(i % n == 0 )
            j = 0;
        //delete whitespaces
        newlines[i] = newlines[i].replace(/\s/g, "");
        result[j].seq += newlines[i];
    }

    return result;
}

function validateStockholm(stockholm){
    var newlines,split_seq, element, aln = [];
    if (!stockholm)
        return false;

    newlines = stockholm.split('\n');
    if(newlines[0].startsWith("# STOCKHOLM 1.0") && newlines.length > 1) {
        //delete empty lines
        newlines = newlines.filter(Boolean);

        for (var i = 1; i < newlines.length; i++) {
            if(newlines[i].startsWith("//")) {
                break;
            }

            if(!newlines[i].startsWith("#")) {

                split_seq = newlines[i].split(/\s/g);
                split_seq = split_seq.filter(Boolean);
                if (split_seq.length < 2) {
                    throw new Error("Sequence or sequence name invalid.");
                    return false;
                }
                element = {};
                element.seq = split_seq[1];

                if (/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(element.seq)) {
                    throw new Error("Alignment contains invalid symbols.");
                    return false;
                }
                aln.push(element);
            }
        }
        return true;
    }
    return false;
}

function json2stockholm(json){
    var result = '';
    result += '# STOCKHOLM 1.0';
    result += "\n";
    result += '#GF SQ ' + json.length;
    result += "\n";
    for (var i = 0; i < json.length; i++) {
        result += '#GF ' + json[i].name.replace(/\s/g, "") +  ' DE ' +  json[i].name ;
        result += "\n";
        result += json[i].name.replace(/\s/g, "");
        result += " \t";
        result += json[i].seq;
        result += "\n";
    }

    return result;
}

function stockholm2json(stockholm) {

    /* TODO: put long ids into description */
    var newlines, element, result = [],split_seq;

    newlines = stockholm.split('\n');
    // remove empty lines
    newlines = newlines.filter(Boolean);
    for (var i = 0; i < newlines.length; i++) {
        if(!newlines[i].startsWith('#')){
            if(newlines[i].startsWith("//"))
                break;
            element = {};
            element.seq = '';
            element.name= '';

            split_seq = newlines[i].split(/\s/g);
            //delete empty whitespace in string array
            split_seq = split_seq.filter(Boolean);
            element.name = split_seq[0];
            element.seq = split_seq[1];
            result.push(element);
        }
    }
    return result;
}



function clustal2json(clustal){

    if(!clustal)
        return false;

    var blockstate, cSeq, k, keys, untrimmed, label, line, lines, match, obj, regex, seqCounter, result, sequence, element;
    if (Object.prototype.toString.call(clustal) === '[object Array]') {
        lines = clustal;
    } else {
        lines = clustal.split("\n");
    }
    result = [];


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
                untrimmed = label;
                sequence = match[2];
                if (seqCounter >= result.length) {
                    obj = getMeta(label);
                    label = obj.name;
                    cSeq = new model(sequence, label, seqCounter);
                    cSeq.untrimmed = untrimmed;
                    cSeq.ids = obj.ids || {};
                    cSeq.details = obj.details || {};
                    keys = Object.keys(cSeq.ids);
                    if (keys.length > 0) {
                        cSeq.id = cSeq.ids[keys[0]];
                    }
                    result.push(cSeq);
                } else {
                    result[seqCounter].seq += sequence;
                }
                seqCounter++;
            } else {
                //console.log("clustal parse error, maybe fasta?", line);
            }
        }
    }

    return result;


}
function json2clustal(clustal){


    var result = [],
        i = 0,
        j =0,
        splitted = [];

    result += "CLUSTAL multiple sequence alignment";
    result += "\n\n";


    for (; j < Math.trunc(clustal[i].seq.length/60) + 1 ; j++){

        for (; i < clustal.length; i++) {
            splitted = clustal[i].name.split(/\s/g);
            result += splitted[0];
            result += "\t";
            result += chunkString(clustal[i].seq, 60)[j];
            result += "\n";

        }

        result += "\n\n";
        i = 0;

    }
    result += "\n"; // hack for codemirror cursor bug with atomic ranges

    return result;

}





function a3m2json(a3m){


    // clean header from multiple occurences of > identifiers, will be missing in output for now.
    var newlines = a3m.split('\n'), element, splittedStrings,i,result;
    for(var k = 0;k < newlines.length;k++){
        if ((newlines[k].match(/>/g)||[]).length > 1) {
            newlines[k] = newlines[k].replace(/(?!^)>/g, '');
        }
    }

    a3m = newlines.join('\n');

    splittedStrings = a3m.split(">");

    result = [];
    i = 1;

    for (; i < splittedStrings.length; i++) {

        element = {};
        element.name = readA3mLine(splittedStrings[i]).name;
        element.seq = readA3mLine(splittedStrings[i]).seq;
        result.push(element);
    }

    return result;

}


function json2a3m(json){
    var result = '';
    for (var i = 0; i < json.length; i++) {
        result += ">";
        result += json[i].name;
        result += "\n";
        result += formatLongSeq(json[i].seq.split('.').join(''));

        result += "\n";
    }

    return result;


}

function validatea3m(a3m) {
    if (!a3m)
        return false;

    if (!validateFasta(a3m)) {

        var newlines = a3m.split('\n');
        newlines = newlines.filter(Boolean);


        var element = fasta2json(a3m);
        for (var i = 0; i < element.length; i++) {
            if(!newlines[i].startsWith(">")){
                return false;
            }

            if (/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(element[i].seq)) {
                return false;
            }


            if (element[i].seq.indexOf('.') > -1) {
                return true;
            }



        }
    }
    return false;
}

function pir2json(pir){

    if (!pir) {
        return false;
    }
    var newlines  = pir.split('\n'),
        //remove empty lines
        newlines = newlines.filter(Boolean);

    var result = [], element;

    for(var i = 0; i < newlines.length;){
        element = {};
        element.name = '';
        if(newlines[i].startsWith('>')) {
            element.name = newlines[i].substring(1);
            element.description += newlines[+i+1];
        }
        i++;
        i++;
        element.seq = '';
        while(i < newlines.length && !newlines[i].startsWith('>')) {
            if(!newlines[i].startsWith(';'))
                element.seq += newlines[i].slice(0, -1);
            i++;
        }
        result.push(element);
    }

    return result;

}
function json2pir(json) {

    var result = '';
    for (var i = 0; i < json.length; i++) {
        result += ">XX;";
        result += json[i].name;
        result += "\n";
        if(json[i].description) {
            result += json[i].description;
            result += "\n";
        }else {
            result += "No description.";
            result += "\n";

        }
        result += formatLongSeq(json[i].seq,60);
        result += '*';
        result += "\n";
    }

    return result;

}
function validatePir(pir){
    if (!pir) {
        return false;
    }

    var element = pir2json(pir);
    for (var i = 0; i < element.length; i++) {

        if (element[i].name.charAt(2) != ";") {
            return false;
        }
        if (/[^\-\\.\\*ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(element[i].seq)) {
            throw new Error("Alignment contains invalid symbols.");
            return false;
        }

    }

    var newlines  = pir.split('>'),
        //remove empty lines
        newlines = newlines.filter(Boolean);

    for (var i = 0; i < newlines.length; i++) {
        // remove whitespace

        newlines[i] = newlines[i].replace(/\s/g, "");
        if (!newlines[i].endsWith('*')) {
            return false;
        }
    }

    return true;
}

function formatLongSeq(seq,n){
    var split, result = "";

    split = seq.match(/.{1,60}/g);

    for (var i= 0; i < split.length; i++){
        result += split[i];
        if(i != split.length -1 )
            result += '\n';
    }
    return result;
}


function validateEMBL(embl) {

    if (!embl) {
        return false;
    }

    var newlines, element, result = [], split = [];

    newlines = embl.split('\n');
    // remove empty lines
    newlines = newlines.filter(Boolean);


    if (newlines[0].startsWith("ID") && newlines[newlines.length-1].endsWith("//")) {
        element = embl2json(embl);

        for (var i = 0; i < element.length; i++) {
            if(element[i].seq == "")
                return false;
            else if(element[i].name == "")
                return false;

            if (/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(element[i].seq)) {
                throw new Error("Alignment contains invalid symbols.");
                return false;
            }
        }
        return true;
    }

    return false;
}

function embl2json(embl){

    if (!embl) {
        return false;
    }

    var  element , result = [];

    var split = embl.split('\n');
    // remove empty lines
    split = split.filter(Boolean);


    for(var i = 0 ;i < split.length ;i++) {
        element = {};
        while (+i+1 < split.length && !split[+i+1].startsWith("ID")) {



            if(split[i].startsWith("ID")) {
                element.name = '';
                element.name += split[i].substring(5);
            }
            if(split[i].startsWith("DE")) {
                element.description = '';
                element.description += split[i].substring(5);
            }
            if(split[i].startsWith("SQ")){
                element.seq = '';

                while(+i+1 < split.length && !split[+i+1].startsWith("//")){
                    i++;
                    element.seq += split[i].replace(/\s/g,"").replace(/[0-9]/g,"").toUpperCase();
                }
            }
            i++;

        }
        result.push(element);

    }
    return result;

}
function json2embl(json){
    var result = '',count;

    for (var i = 0; i < json.length; i++) {
        result += "ID   ";
        result += json[i].name.split(/\s/g)[0] +  "; " + "; "  + "; "  + "; "  + "; "  + json[i].seq.length + " BP.";
        result += "\n";
        result += "XX";
        result += "\n";
        // deleting the id that is already printed to results in order to
        // print a long id into description
        var splitName = json[i].name.split(/\s/g);
        splitName.shift();
        splitName = splitName.join(" ");


        if(splitName != "" || json[i].description) {

            result += "DE   ";
            result += splitName;
            if(json[i].description)
                result += " " + json[i].description;
            result += "\n";
            result += "XX";
            result += "\n";
        }


        count = aminoCount(json[i].seq);
        result += "SQ   Sequence " + json[i].seq.length + " BP; " ;
        result += count['A'.charCodeAt()] + " A; ";
        result += count['C'.charCodeAt()] + " C; ";
        result += count['G'.charCodeAt()] + " G; ";
        result += count['T'.charCodeAt()] + " T; ";

        var others = 0;
        for(var j =0 ; j < count.length; j++){
            if(j != 'A'.charCodeAt() && j != 'C'.charCodeAt() && j != 'G'.charCodeAt() && j != 'T'.charCodeAt()){
                others += count[j];
            }
        }
        result += others + " others; ";
        result += "\n";
        result += formatEmblSeq(json[i].seq).toLowerCase();
        result += "\n";
        result += "//";
        result += "\n";

    }


    return result;

}

function formatEmblSeq(seq){
    var split, result = "", charCount;

    split = seq.match(/.{1,60}/g);
    charCount = 0;
    for (var i= 0; i < split.length; i++){
        result += "     " + addSpaceEveryNChars(split[i],10);

        //calculate number of whitespaces
        var numOfSpace = 80 - split[i].length - 5 - (charCount + split[i].length).toString().length - ((''+(split[i].length-1))[0]);

        // to have count formatted at the end of the line
        for (var j = 0; j < numOfSpace ;j++) {
            result += " ";
        }

        charCount =  charCount + split[i].length;
        result += charCount;
        if(i != split.length -1 )
            result += '\n';
    }
    return result;
}


function validateNexus(nexus) {

    if (!nexus) {
        return false;
    }
    var element, ntax, nchar;
    var split = nexus.split('\n');
    // remove empty lines
    split = split.filter(Boolean);
    if(!nexus.startsWith("#NEXUS"))
        return false;
    if(!split[1].toUpperCase().startsWith("BEGIN"))
        return false;
    if(!split[split.length - 1].toUpperCase().startsWith("END;"))
        return false;

    element = nexus2json(nexus);

    //get dimensions
    var extractNumbers = split[2].toUpperCase().replace("NTAX="," ").replace("DIMENSIONS", "").replace("NCHAR="," ").replace(";"," ");

    ntax = extractNumbers.split(/\s/g).filter(Boolean)[0];
    nchar= extractNumbers.split(/\s/g).filter(Boolean)[1];

    //check dimensions
    if( ntax!= element.length) {
        throw new Error("Number of sequences does not match with the header.");
        return false;
    }
    for(var i = 0; i < element.length; i++){
        if( nchar!= element[i].seq.length) {
            throw new Error("Number of sequences does not match with the header.");
            return false;
        }
    }


    for(var i =0; i < element.length; i++){
        if(element[i].name = "")
            return false;
        if(element[i].seq = "" )
            return false;

    }

    return true;
}

function nexus2json(nexus){
    if (!nexus) {
        return false;
    }

    var  element , result = [];

    var split = nexus.split('\n'), seq =[];
    // remove empty lines
    split = split.filter(Boolean);
    split.shift();
    for(var i = 0 ;i < split.length ;i++) {


        if(split[i].toUpperCase().startsWith("MATRIX")){
            while(+i+1 < split.length && !split[+i+1].startsWith(";")){
                element = {};
                i++;
                element.name = split[i].split(/\s/g)[0];
                seq = split[i].split(/\s/g);
                seq.shift();
                seq.filter(Boolean);
                element.seq = seq.join("").replace(/\?/g, "-").toUpperCase();
                result.push(element);

            }
            return result;
        }

    }
}

function json2nexus(json){
    var result = '';
    result += "#NEXUS";
    result += "\n";
    result += "Begin data;";
    result += "\n";
    result += "Dimensions ntax=" + json.length + " nchar=" + json[0].seq.length + ";";
    result += "\n";
    result += "format datatype=" + typeOfSequence(json) + " missing=? gap= -;";
    result += "\n";
    result += "Matrix";
    result += "\n";
    for (var i = 0; i < json.length; i++) {
        result += json[i].name.replace(/\s/g,"");
        result += "\t";
        result += json[i].seq.toLowerCase();
        result += "\n";
    }
    result += ";";
    result += "\n";
    result += "End;";


    return result;

}


function validateGenbank(genbank){
    if (!genbank) {
        return false;
    }
    var element;
    var split = genbank.split('\n');
    // remove empty lines
    split = split.filter(Boolean);
    if(!genbank.toUpperCase().startsWith("LOCUS"))
        return false;
    if(!split[split.length-1].toUpperCase().replace(/\s/g,"").endsWith("//"))
        return false;

    element = genbank2json(genbank);

    for(var i =0; i < element.length; i++){
        if(element[i].name = "")
            return false;
        if(element[i].seq = "" )
            return false;

    }

    return true;
}

function genbank2json(genbank){
    if (!genbank) {
        return false;
    }

    var  element , result = [] , accession, locus , definition;

    var split = genbank.split('\n');
    // remove empty lines

    split = split.filter(Boolean);
    for(var i = 0 ;i < split.length ;i++) {
        element = {};
        element.name = "";
        element.seq = "";
        element.description = "";
        element.accession = "";


            if(split[i].toUpperCase().startsWith("ACCESSION"))
                element.accession = " " + split[i].replace("ACCESSION", "").trim();
            if(split[i].toUpperCase().startsWith("DEFINITION"))
                definition = " " + split[i].replace("DEFINITION", "").trim();
            if(split[i].replace(" ", "").toUpperCase().startsWith("ORIGIN")){
                while(+i+1 < split.length && !split[+i+1].startsWith("//")){
                    i++;
                    element.seq += split[i].replace(/\s/g,"").replace(/[0-9]/g,"").toUpperCase();
                }
                element.name += (element.accession + " " + definition).trim();



            i++;
            result.push(element);
        }

    }
    return result;
}

function json2genbank(json){
    var result = '';

    for (var i = 0; i < json.length; i++) {
        result += 'LOCUS\t\t\t\t\t\t' + json[i].seq.length + " bp\t\t\t" + typeOfSequence(json[i].seq) ;
        result += '\n';
        result += 'DEFINITION\t\t';
        result += json[i].name;
        result += '\n';
        result += 'ACCESSION\t\t';
        if(json[i].accession) {
            result += json[i].accession;
        }else{
            result += json[i].name.substring(0,10);
        }
        result += '\n';
        result += 'VERSION\t\t'
        result += '\n';
        result += 'KEYWORDS\t\t'
        result += '\n';
        result += 'SOURCE\t\t'
        result += '\n';
        result += 'ORGANISM\t\tLocation/Qualifiers';
        result += '\n';
        result += 'ORIGIN';
        result += '\n';

        var splitSeq =  json[i].seq.toLowerCase().match(/.{1,60}/g);
        var count = 0;
        for(var j = 0; j < splitSeq.length; j++){
        count = splitSeq[j].length + count;
            for (var k = 0; k < 9 - (count.toString().length);k++) {
                result += " ";
            }
            // print num of sequence
            result += count;
            result += " ";
            result += addSpaceEveryNChars(splitSeq[j],10);
            result += '\n';
        }

        result += '//';
        result += '\n';

    }

    return result;

}


function searchRegex(text, regex, flag) {
    if (!text) {
        return false;
    }
    if (regex=="") {
        return false;
    }

    var  result = [] , matches, split, reg, beginHit,endHit, tmp,lastBeginHit, lastEndHit;

    split = text.split('\n');
    // remove empty lines

    split = split.filter(Boolean);

    reg =  new RegExp(regex, flag) ;
    if(reg != null) {

        for (var i = 0; i < split.length; i++) {

            if (split[i].match(reg) != null) {
                beginHit =-2;
                endHit = -2;

                result += i + '\t\t\t';
                matches = split[i].match(reg);
                console.log("matches" + matches)
                tmp = split[i];
                lastBeginHit = 0;
                lastEndHit = 0;
                for(var j=0; j < matches.length; j++) {
                    beginHit = tmp.indexOf(matches[j],endHit+2);
                    endHit = tmp.indexOf(matches[j],endHit+2) + matches[j].length -1;
                    tmp =  tmp.substr(0,beginHit) + '\'' + tmp.match(reg)[j] + '\'' +  tmp.substr(endHit+1);
                    lastBeginHit = beginHit;
                    lastEndHit = endHit;
                }
                result += tmp;
                result += '\n';

            }
        }
        return result;
    }
    return false;


}

function typeOfSequence(json) {
    if (!/[^\-\\.AGUC\s]/i.test(json[0].seq)){
        return "RNA"
    }

    if (!/[^\-\\.AGTC\s]/i.test(json[0].seq)){
        return "DNA"
    }
    if (!/[^\-\\.ABCDEFGHIKLMNPQRSTUVWXYZ\s]/i.test(json[0].seq)){
        return "Protein"
    }
    return  "undefined";

}