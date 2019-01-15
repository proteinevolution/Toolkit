/* tslint:disable:max-line-length */

export default {
    overview: `The Backtranslator translates an amino acid sequence back into a possible DNA sequence.`,
    parameters: [
        {
            title: 'Input',
            content: `Enter a single protein sequence in FASTA format.<br/><br/>
                @:toolHelpModals.common.singleseq`,
        },
        {
            title: 'Include amino acid sequence in output',
            content: `<p>Activate this option to include the amino acid sequence in the output. If activated amino acids will be printed above their encoding DNA codon. For example, results may look like this:</p><br/>
                    <pre>> Possible DNA-sequence of Protein: gi|576099|pdb|1ENA|  Staphylococcal Nuclease (E.C.3.1.31.1) Mutation<br/> L   H   K   E   P   A   T   L   I   K   A   I   D   G   E   T   V   K   L   M  <br/>TTG CAC AAG GAG CCT GCA ACC CTT ATC AAG GCA ATA GAC GGA GAA ACG GTA AAA CTC ATG <br/><br/> Y   K   G   Q   P   M   T   F   R   L   L   L   V   D   T   P   E   T   K   H  <br/>TAC AAA GGG CAG CCT ATG ACG TTT CGG CTA CTT CTG GTC GAC ACG CCG GAA ACG AAA CAT <br/><br/> P   K   K   G   V   E   K   Y   G   P   E   A   S   A   F   T   K   K   M   V  <br/>CCG AAG AAA GGG GTC GAA AAA TAT GGT CCG GAG GCC TCG GCA TTC ACA AAA AAG ATG GTA <br/><br/> E   N   A   K   K   I   E   V   E   F   D   K   G   Q   R   T   D   K   Y   G  <br/>GAG AAT GCT AAA AAA ATT GAG GTG GAG TTT GAT AAA GGT CAA CGA ACT GAC AAG TAT GGT <br/><br/> R   G   L   A   Y   I   Y   A   D   G   K   M   V   N   E   A   L   V   R   Q  <br/>AGA GGT TTA GCT TAC ATC TAC GCT GAT GGC AAG ATG GTC AAT GAA GCC CTC GTT CGG CAA <br/><br/> G   L   A   K   V   A   Y   V   Y   K   P   N   N   T   H   E   Q   H   L   R  <br/>GGC CTT GCA AAA GTG GCT TAT GTG TAT AAA CCG AAC AAT ACT CAC GAA CAA CAC CTT CGG <br/><br/> K   S   E   A   Q   A   K   K   E   K   L   N   I   W   S  <br/>AAA TCA GAG GCT CAG GCA AAA AAG GAA AAA TTA AAT ATC TGG TCG </pre><br/><br/>With the option turned of the above output would look like this:<br/><br/><br/><pre>> Possible DNA-sequence of Protein: gi|576099|pdb|1ENA|  Staphylococcal Nuclease (E.C.3.1.31.1) Mutation <br/>CTTCATAAAGAACCAGCTACCCTTATCAAGGCGATCGACGGTGAAACTGTGAAGCTCATGTACAAGGGGCAAC<br/>CAATGACGTTCCGCCTGCTGCTTGTGGACACACCCGAAACTAAGCACCCCAAAAAAGGTGTAGAGAAATACGG<br/>GCCTGAAGCTTCTGCCTTCACAAAAAAGATGGTGGAAAACGCAAAGAAGATTGAGGTCGAATTCGATAAGGGC<br/>CAAAGAACGGATAAGTACGGTCGAGGGCTGGCCTATATTTACGCTGACGGCAAGATGGTGAATGAAGCGCTCG<br/>TGCGTCAAGGACTGGCCAAAGTAGCATACGTTTACAAACCAAATAATACTCATGAGCAACACCTCCGAAAGAG<br/>TGAGGCGCAAGCTAAAAAGGAAAAGCTCAACATCTGGTCC</pre>`,
        },
        {
            title: 'Choose a genetic Code',
            content: `Select the correct genetic code for your organism. The genetic codes are identical to those available
                on the <a href = https://www.ncbi.nlm.nih.gov/taxonomy target="_blank" rel="noopener">NCBI homepage</a>.`,
        },
        {
            title: 'Use codon usage table of',
            content: `Enter the species name (e.g. Homo sapiens, Escherichia coli) you want to use the codon table of.
                    If this option is not used all possible codons will have the same probability.
                    Check at the result page that the correct codon table has been used.
                    You will see a message like:
                    <pre>
                    Generated using codon usage table of: Homo sapiens
                    </pre>`,
        },
    ],
    references: `<p>Nakamura Y., Gojobori T., Ikemura T. (2000) <b>Codon usage tabulated from international DNA sequence
                databases: status for the year 2000. </b>Nucleic Acids Res 28(1):292.
                <a href = https://www.ncbi.nlm.nih.gov/pubmed/10592250 target="_blank" rel="noopener">PMID: 10592250</a></p>`,
};
