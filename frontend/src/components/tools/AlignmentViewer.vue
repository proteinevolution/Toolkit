<template>
    <div>
        <div ref="menu"></div>
        <div ref="root">Loading...</div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'AlignmentViewer',
        props: {
            sequences: String,
            format: String,
        },
        mounted() {
            const seqs = this.$msa.io[this.format].parse(this.sequences);
            if (!seqs) {
                return;
            }

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
            const msa = new this.$msa.msa(opts);

            const menuOpts = {
                el: this.$refs.menu,
                msa,
            };
            const defMenu = new this.$msa.menu.defaultmenu(menuOpts);
            msa.addView('menu', defMenu);

            msa.render();

            /*
            $(window).on("resize.MSAViewerClustal", function () {
                if ($("#rootDiv").parents("html").length === 0) {
                    $(window).off("resize.MSAViewerClustal");
                    return;
                }
                alignmentItem.g.zoomer.set("alignmentWidth", toolTabs.width() - 240);
                if (toolTabs.hasClass('fullscreen')) {
                    alignmentItem.g.zoomer.set("alignmentHeight", Math.max(400, $(window).height() - 320));
                }
            });
            */
        },
    });
</script>

<style lang="scss" scoped>
</style>
