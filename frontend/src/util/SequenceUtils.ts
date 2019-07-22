import {PatsearchMatch} from '@/types/toolkit/results';

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
        case 'coils_w28':
        case 'pcoils_w28':
            return seq.toUpperCase().replace(/[CX]/g, (m) => {
                if (m === 'X') {
                    return '&nbsp;';
                }
                return '<span class="CC_b">' + m + '</span>';
            });
        case 'tmhmm':
        case 'phobius':
        case 'polyphobius':
            return seq.toUpperCase().replace(/[MX]/g, (m) => {
                if (m === 'X') {
                    return '&nbsp;';
                }
                return '<span class="CC_m">' + m + '</span>';
            });
        case 'pipred':
            return seq.toUpperCase().replace(/[IX]/g, (m) => {
                if (m === 'X') {
                    return '&nbsp;';
                }
                return '<span class="ss_pihelix">' + m + '</span>';
            });
    }
    return 'Error! Unknown tool';
}

export function patsearchColor(seq: string, matches: PatsearchMatch[]): string {
    let res: string = '';
    let end: number = 0;
    for (const match of matches) {
        res += seq.slice(end, match.i);
        res += '<span class="pattern-match">' + seq.slice(match.i, match.i + match.n) + '</span>';
        end = match.i + match.n;
    }
    res += seq.slice(end, seq.length);
    return res;
}
