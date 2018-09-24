declare module 'vue-multiselect';
declare module 'velocity-animate';
declare module 'vue-particles';
declare module 'msa' {
    interface MSA {
        render: () => void;
        addView: (name: string, menu: any) => void;
    }

    interface MsaAPI {
        msa: {
            new(options: any): MSA;
        };
        menu: {
            defaultmenu: {
                new(options: any): void;
            },
        };
        io: any;
    }

    const msa: MsaAPI;
    export = msa;
}
