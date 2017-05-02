/// <reference path="sampleSeq.ts"/>


interface Window {
    call404 : any;
}

window.call404 = function(elem : any, isInit: boolean) : any {

    // msa viewer for 404 page
    if(!isInit) {
        let ErrorOpts = {
            colorscheme: {
                "scheme": "taylor"
            },
            el: document.getElementById("404msa"),
            vis: {
                conserv: false,
                overviewbox: false,
                seqlogo: false,
                markers: false,
                labels: false,
                labelName: false,
                labelId: false,
                labelPartition: false,
                labelCheckbox: false
            },
            menu: false,
            bootstrapMenu: false,
            zoomer: {
                alignmentHeight: 500,
                autoResize: true
            },

            //seqs: fasta2json(multiProtSeq)
            seqs : fasta2json($('#hidden404').val())

        };

        //console.log($('#hidden404').val());
        let a = new msa.msa(ErrorOpts);
        a.render();

    }

};
