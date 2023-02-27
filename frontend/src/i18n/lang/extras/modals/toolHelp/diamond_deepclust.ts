/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            diamond_deepclust: {
                overview: `DIAMOND-DeepClust clusters protein sequences analogous to CD-HIT or UCLUST based on a user-defined 
                clustering criterion, finding a set of centroid or representative sequences and assigning each input 
                sequence to the cluster of one representative such that the clustering criterion vs. the representative 
                is fulfilled. The clustering criterion is defined by sequence coverage of the local alignment as well 
                its sequence identity (see below). Note that due to the heuristic nature of the cascaded clustering 
                algorithm, these cutoff values serve to guide the computation, but their fulfillment is not always 
                guaranteed.<br><br>For more details, please refer to the
                <a href="https://github.com/bbuchfink/diamond/wiki/Clustering" target="_blank" rel="noopener">
                GitHub page of DIAMOND</a>.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `@:toolHelpModals.common.multiseq`,
                    },
                    {
                        title: 'Min. approx. identity% to cluster sequences',
                        content: `List matches above this sequence identity (for clustering)`,
                    },
                    {
                        title: 'Min. cov% of the cluster member sequence',
                        content: `List matches above this fraction of aligned (covered) query and target residues`,
                    },
                ],
                references: `<p>Sensitive clustering of protein sequences at tree-of-life scale using DIAMOND DeepClust.<br> 
                Buchfink B, Ashkenazy H, Reuter K, Kennedy JA, Drost HG. <a href="https://doi.org/10.1101/2023.01.24.525373" 
                target="_blank" rel="noopener"> bioRxiv 2023.01.24.525373</a>.</p>`,
            },
        },
    },
};
