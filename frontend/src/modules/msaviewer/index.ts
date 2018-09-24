import Vue, {VueConstructor} from 'vue';

const msa = require('msa');

const MsaViewer = {
    install(vconst: VueConstructor, args: any = {}) {
        vconst.prototype.$msa = msa;
    },
};

export default MsaViewer;
