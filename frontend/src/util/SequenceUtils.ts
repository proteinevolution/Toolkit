export function colorSequence(seq: string): string {
    return seq.replace(/(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)/g, (m) => {
        return '<span class="aa_' + m.toString()[0] + '">' + m.toString() + '</span>';
    });
}

export function quick2dColor(name: string, seq: string): string {
    switch (name) {
        case 'psipred':
        case 'spider':
        case 'psspred':
        case 'deepcnf':
        case 'netsurfpss':
            return seq.toUpperCase().replace(/[CHE]/g, (m) => {
                if (m === 'C') {
                    return '&nbsp;';
                }
                return '<span class="ss_' + m.toLowerCase() + '_b">' + m + '</span>';
            });
        case 'netsurfpd':
        case 'spot-d':
        case 'iupred':
        case 'disopred':
            return seq.toUpperCase().replace(/[DO]/g, (m) => {
                if (m === 'O') {
                    return '&nbsp;';
                }
                return '<span class="CC_do">' + m + '</span>';
            });
        case 'marcoil':
        case 'coils':
        case 'pcoils':
            return seq.toUpperCase().replace(/[CX]/g, (m) => {
                if (m === 'X') {
                    return '&nbsp;';
                }
                return '<span class="CC_b">' + m + '</span>';
            });
    }
    return 'waiting';
}

// def Q2DColorReplace(name: String, sequence: String): String =
//     name match {
//       case "tmhmm"       => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
//       case "phobius"     => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
//       case "polyphobius" => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
//       case "pipred"      => PIHELIX_pattern.replaceAllIn(sequence, "<span class=\"ss_pihelix\">" + "$1" + "</span>")
//
//     }
