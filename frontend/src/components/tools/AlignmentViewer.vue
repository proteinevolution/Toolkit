<template>
    <div ref="container">
        <div ref="menu"></div>
        <div ref="root"
             v-text="$t('tools.alignmentViewer.loading')"></div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {MSAViewerSeq} from '@/types/toolkit/tools';
    import Logger from 'js-logger';
    import {AlignmentItem} from '@/types/toolkit/results';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('AlignmentViewer');

    export default Vue.extend({
        name: 'AlignmentViewer',
        props: {
            sequences: {
                type: [String, Array],
                required: false,
            },
            format: {
                type: String,
                required: false,
                default: 'fasta',
            },
        },
        data() {
            return {
                msaViewer: null as any,
            };
        },
        computed: {
            seqs(): MSAViewerSeq[] | undefined {
                const t: string = typeof this.sequences;
                switch (t) {
                    case 'undefined':
                        return undefined;
                    case 'string':
                        return msa.io[this.format].parse(this.sequences);
                    case 'object':
                        return (this.sequences as AlignmentItem[]).map((a: AlignmentItem) => {
                            return {
                                name: a.accession,
                                id: a.num.toString(),
                                seq: a.seq,
                            };
                        });
                    default:
                        logger.error(`invalid type for sequences "${t}"`);
                        return undefined;
                }
            },
        },
        mounted() {
            EventBus.$on('alignment-viewer-resize', (fullScreen: boolean) => {
                this.$nextTick(() => {
                    this.autoResize(fullScreen);
                });
            });
            if (this.seqs) {
                this.buildMSAViewer(this.seqs);
            }
        },
        beforeDestroy() {
            EventBus.$off('alignment-viewer-resize');
        },
        methods: {
            buildMSAViewer(seqs: MSAViewerSeq[]) {
                const opts = {
                    colorscheme: {
                        scheme: 'clustal',
                    },
                    el: this.$refs.root,
                    seqs,
                    vis: {
                        conserv: false,
                        overviewbox: false,
                        seqlogo: true,
                        labels: true,
                        labelName: true,
                        labelId: false,
                        labelPartition: false,
                        labelCheckbox: false,
                    },
                    conf: {
                        dropImport: true,
                    },
                    zoomer: {
                        // Alignment viewer is not scrolling with 'alignmentWidth: "auto"', use fixed numbers instead or
                        // use script for handling
                        alignmentHeight: 400,
                        alignmentWidth: 400,
                        labelNameLength: 165,
                        labelWidth: 85,
                        labelFontsize: '13px',
                        labelIdLength: 75,
                        menuFontsize: '13px',
                        menuPadding: '0px 10px 0px 0px',
                        menuMarginLeft: '-6px',
                        menuItemFontsize: '14px',
                        menuItemLineHeight: '14px',
                        autoResize: false,
                    },
                };
                this.msaViewer = new msa(opts);

                const menuOpts = {
                    el: this.$refs.menu,
                    msa: this.msaViewer,
                };
                const defMenu = new msa.menu.defaultmenu(menuOpts);
                this.msaViewer.addView('menu', defMenu);

                this.msaViewer.render();
            },
            autoResize(fullScreen: boolean) {
                const parent: HTMLElement = (this.$refs.container as HTMLElement);
                if (this.msaViewer && parent) {
                    this.msaViewer.g.zoomer.set('alignmentHeight', fullScreen ? window.innerHeight - 500 : 300);
                    this.msaViewer.g.zoomer.set('alignmentWidth', parent.offsetWidth - 180);
                }
            },
        },
        watch: {
            seqs: {
                immediate: true,
                handler(seqs: MSAViewerSeq[] | undefined): void {
                    if (!seqs) {
                        return;
                    }
                    this.buildMSAViewer(seqs);
                },
            },
        },
    });
</script>

<style lang="scss">
    .biojs_msa_div {
        .smenubar {
            height: 40px;

            a.smenubar_alink {
                color: #737373;
                padding: 4px 12px 4px 0 !important;
                font-weight: 700;
                background: none;
                cursor: pointer;
            }

            // hide Debug Menu Item
            & > div:last-of-type {
                display: none !important;
            }
        }

        .biojs_msa_labelblock .biojs_msa_labelrow {
            font-weight: normal;
        }

        .biojs_msa_searchresult_row > button {
            margin-right: 0.65rem;
            margin-left: 0.1rem;
            font-size: 0.7em !important;
            background-color: #7b7b7b;
            border: none;
            color: white;
            padding: 5px 18px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
        }

        .biojs_msa_searchresult_ovbox {
            display: none
        }
    }
</style>
