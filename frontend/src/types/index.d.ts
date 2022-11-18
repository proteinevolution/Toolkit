declare module 'vue-multiselect';
declare module 'velocity-animate';
declare module 'vue-simple-spinner';
declare module 'vue-switches';
declare module 'vue-native-websocket';
declare module 'vue-cookie-accept-decline';
declare module 'handy-scroll';
declare module 'tidytree';
// declare module 'msa' {
//     interface MSA {
//         render: () => void;
//         addView: (name: string, menu: any) => void;
//     }
//
//     const msa: {
//         menu: {
//             defaultmenu: {
//                 new(options: any): void;
//             },
//         };
//         io: any;
//         msa: {
//             new(options: any): MSA;
//         }
//     };
//     export = msa;
// }
declare const msa: any;

// Provide vue typings for Vue2 compatibility since they will no longer be present with Vue3
// https://v3-migration.vuejs.org/migration-build.html#upgrade-workflow
declare module 'vue' {
    import { CompatVue } from '@vue/runtime-dom';
    const Vue: CompatVue;
    export default Vue;
    export * from '@vue/runtime-dom';
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    const { configureCompat } = Vue;
    export { configureCompat };
}
