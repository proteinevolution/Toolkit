/// <reference path="sampleseq.ts"/>


interface Window {
    call404: any;
}

window.call404 = function(elem: any, isInit: boolean): any {

    // msa viewer for 404 page
    if (!isInit) {
        const seqs = document.getElementById("hidden404") !== null ? $("#hidden404").val() : ">PMN59974.1 histRne H2H [HWmN sapiens]\n" +
            "-------TCATARAEV-------QETPELNKH-----------QKVAVEDRE---\n" +
            ">SFN59974.1 histMne H2A [HVmA sapiens]\n" +
            "------HNTGFHWVIY-----FCRMIITFHTDS---------QRKPGNGMEH---\n" +
            ">ARN59974.1 histHne H2S [HCmN sapiens]\n" +
            "-----MDEFVNNMIDS---QAWYDGYKNAIEPMIGG------KEAFLGPLEHC--\n" +
            ">LCN59974.1 histMne H2K [HEmK sapiens]\n" +
            "----NWIMNIRERCCQ--FTNTNKTYYLWRLMYCRWR----TWNGVGTINGAM--\n" +
            ">KQN59974.1 histVne H2P [HVmQ sapiens]\n" +
            "---HFYYKE-SPPSDR--SAMHCIYV---LGHPCFQN---DTYSPR-YNSYTA--\n" +
            ">TCN59974.1 histAne H2Q [HNmT sapiens]\n" +
            "--MTACHA--CHTCVE--QCDRHWT-----DREPAGR--RWFFAM--ITCQEL--\n" +
            ">SWN59974.1 histRne H2A [HRmH sapiens]\n" +
            "-EHLKFK---SICLIT--SAEYQWS-----DRKFINC-WDSEQH---SMEVMM--\n" +
            ">WPN59974.1 histIne H2E [HSmT sapiens]\n" +
            "VDWKDWGSGLIHWPARNMAYMYMRG-GFR-SAIIYSSFEVTLFKPTLMPSHFCWI\n" +
            ">PYN59974.1 histYne H2M [HRmE sapiens]\n" +
            "LIIRFANFGQYEISDDPDNEDLRLF-PVK-SSHCFFNFMRPVCGDTNVTDRGEWY\n" +
            ">KIN59974.1 histGne H2E [HRmI sapiens]\n" +
            "AQGESNTQEHQCWHVPLTVDSHFCD-----GLVEVPNPPTLMWTPDEPFFRMQQE\n" +
            ">APN59974.1 histCne H2Y [HFmF sapiens]\n" +
            "----------QNKDEC--TFIRAPM-----DWQGCPH----------GHQIYE--\n" +
            ">CWN59974.1 histNne H2E [HQmA sapiens]\n" +
            "----------PESLTP--WKLFNCIP---RDDINRID----------ISARCF--\n" +
            ">TSN59974.1 histTne H2K [HKmY sapiens]\n" +
            "----------SDPLPA--IDHKWWFCQVYMEGTFARG----------WHVVFL--\n" +
            ">IGN59974.1 histCne H2S [HWmE sapiens]\n" +
            "--------YHCNLHLGIF-MWDWLQCDFCQLRHILY---------FGCMTWDYVY\n" +
            ">FCN59974.1 histVne H2S [HPmS sapiens]\n" +
            "--------QACYQWAWPV---VMMTWHLLQECHE-----------NMDFARGWGV\n" +
            ">QCN59974.1 histAne H2S [HTmC sapiens]\n" +
            "--------MGHWSDPHMN-----KLNPMGWNV-------------YDFPFARPCL";
        const ErrorOpts = {
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
            seqs: fasta2json(seqs)

        };

        const a = new msa.msa(ErrorOpts);
        a.render();
        TitleManager.updateTitle(["404"]);
    }

};
