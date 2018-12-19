declare module 'vue-multiselect';
declare module 'velocity-animate';
declare module 'vue-particles';
declare module 'vue-clipboard2';
declare module 'vue-simple-spinner';
declare module 'vue-switches';
declare module 'vuetable-2/src/components/Vuetable';
declare module 'msa' {
    interface MSA {
        render: () => void;
        addView: (name: string, menu: any) => void;
    }

    const msa: {
        menu: {
            defaultmenu: {
                new(options: any): void;
            },
        };
        io: any;
        msa: {
            new(options: any): MSA;
        }
    };
    export = msa;
}
