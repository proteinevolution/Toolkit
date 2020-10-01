/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            patsearch: {
                overview: `Retrieves all sequences in the chosen database which contain a user-defined PROSITE pattern or regular expression.
        For help on constructing the pattern, please refer to the 'Input & Parameters' section. In the output, the pattern
        in the found sequences are highlighted in red.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `<pre>Prosite grammar:<br/><br/>Example:<br/>G[VLIM](2)x(3,10)W{A}Dxx[ST]<br/><br/>A prosite pattern
                        is described using the following conventions:<br/><br/>-> The standard IUPAC one-letter codes for
                        the amino acids are used.<br/>-> The symbol 'x' is used for a position where any amino acid is accepted. <br/>
                        -> Ambiguities are indicated by listing the acceptable amino acids for a given position, <br/>
                        between square parentheses '[ ]'. For example: [ALT] stands for Ala or Leu or Thr. <br/>
                        -> Ambiguities are also indicated by listing between a pair of curly brackets '{ }' the <br/>
                        amino acids that are not accepted at a given position. For example: {AM} stands for any <br/>
                        amino acid except Ala and Met. <br/>-> Repetition of an element of the pattern can be indicated
                        by following that element with <br/>   a numerical value or a numerical range between parenthesis. <br/>
                        Examples: x(3) corresponds to xxx, x(2,4) corresponds to xx or xxx or xxxx. <br/>
                        -> When a pattern is restricted to either the N- or C-terminal of a sequence, <br/>
                        that pattern either starts with a '<' symbol or respectively ends with a '>' symbol. <br/>
                        In some rare cases (e.g. PS00267 or PS00539), '>' can also occur inside square brackets for the <br/>
                        C-terminal element. 'F[GSTV]PRL[G>]' means that either 'F[GSTV]PRLG' or 'F[GSTV]PRL>' are considered.
                        <br/><br/><br/>Regular expression:<br/><br/>Example:<br/>G[VLIM]{2}.{3,10}W[^A]D..[ST]<br/><br/>
                        -> A character class, defined by square parentheses '[ ]', matches a single amino acid out <br/>
                        of all the possibilities offered by the character class.<br/>   For example: [ALT] stands for Ala or Leu or Thr<br/>
                        -> A caret '^' immediately after the opening '[' negates the character class.<br/>
                        For example: [^ALT] stands for all amino acid except Ala and Leu and Thr<br/>-> A dot '.'
                        matches any amino acid.<br/>-> A pair of curly brackets '{}' repeats the previous item exactly
                        n times '{n}' <br/>   or between n and m times '{n,m}'.<br/>   For example: A{3} correspond to
                        AAA, .{2,4} corresponds to .. or ... or ....<br/>-> A caret '^' match the start of the sequence.<br/>
                        -> A dollar '$' match the end of the sequence.</pre>`,
                    },
                    {
                        title: 'Select target database',
                        content: `
                    The selected standard or proteome database is used in the search.
                        <ul>
                            <li><b>nr:</b> the nonredundant sequence database at the NCBI.</li>
                            <li><b>env:</b> NCBI's env database with environmental sequences, usually from unknown organisms.</li>
                            <li><b>nre:</b> nr + env database.</li>
                            <li><b>nr50, nr70, nr90:</b> NCBI's nonredundant protein sequence database filtered for a
                                maximum pairwise sequence identity of 50%, 70% and 90%, respectively.</li>
                            <li><b>env70, env90:</b> NCBI's environmental sequence database, usually from unknown organisms,
                                filtered for a maximum pairwise sequence identity of 70% and 90%, respectively.</li>
                            <li><b>nr_euk*, nr_bac*, nr_arc*, nr_pro*, nr_vir*:</b> eukaryotic, bacterial, archaeal,
                                prokaryotic, and viral sequences from the nonredundant database nr.</li>
                            <li><b>pdb_nr: </b>Sequences from proteins whose structures have been deposited in the Protein
                                Data Bank PDB.</li>
                            <li><b>Uniprot_sprot:</b> manually annotated and reviewed sequences from the
                                <a href="http://www.expasy.org/sprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                            <li><b>Uniprot_trembl:</b> automatically annotated and not reviewed sequences from the
                                <a href="http://www.ebi.ac.uk/swissprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                        </ul>
                `,
                    },
                    {
                        title: 'Select grammar',
                        content: `Specify the format of your pattern.`,
                    },
                    {
                        title: 'Max target hits',
                        content: `You can select the max. count of sequences that will be fetched by your Pattern Search.`,
                    },
                ],
                references: ``,
            },
        },
    },
};
