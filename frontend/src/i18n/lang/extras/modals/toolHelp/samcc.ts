/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            samcc: {
                overview: `SamCC measures local parameters of (a)symmetric, parallel, and antiparallel four-helical bundles.
    Briefly, SamCC divides bundle into layers (slices), brings each layer into an idealized state
    (expected from equations), then performing final calculations on the resulting idealized structure.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `A PDB file containing a four-helical bundle motif. Missing or unusual residues, like selenomethionine,
            are not allowed. The structure should not contain alternative conformations.`,
                    },
                    {
                        title: 'Definition line for helix 1-4',
                        content: `<p>In these four lines, every helix of a four helical bundle is defined. In each line you
                need to enter the chain identifier of each helix (one character or digit) and its start and end
                positions (use PDB file numbering). Residues localized at positions defined by "from" values form
                the first layer of the bundle, while those in "to" the last one. You also need to provide the
                register of residues located in the first defined layer (one letter, a-g for 7 periodicity,
                a-k for 11 periodicity etc.). The registers of subsequent layers are automatically defined based
                on periodicity and helix direction (parallel vs antiparallel).</p>
                    <p>IMPORTANT NOTE #1: If the "from" value is larger than "to" then program assumes
                    that given helix is antiparallel (see also Example 2).</p>
                    <p>IMPORTANT NOTE #2: Helices pairs 1&2 and 3&4 have to be located on
                    the diagonals (see Figures 1 and 2).</p>
                `,
                    },
                    {
                        title: 'Periodicity',
                        content: `The program needs to know the periodicity in order to calculate Crick angle deviation
            (observed Crick angle - expected Crick angle).`,
                    },
                ],
                references: `<p>Dunin-Horkawicz S., Lupas AN. (2010)
        <b>Measuring the conformational space of square four-helical bundles with the program samCC. </b>
        170(2):226-35. doi: 10.1016/j.jsb.2010.01.023.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/20139000 target="_blank" rel="noopener">PMID: 20139000</a></p>`,
            },
        },
    },
};
