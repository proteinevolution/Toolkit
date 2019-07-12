export function colorSequence(seq: string) {
    return seq.replace(/(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)/g, (m) => {
        return '<span class="aa_' + m.toString()[0] + '">' + m.toString() + '</span>';
    });
}
