<template>
    <div>
        <alignment-viewer :sequences="job.alignments"/>

        <tool-citation-info :tool="tool"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Job} from '@/types/toolkit/jobs';
    import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
    import {Tool} from '@/types/toolkit/tools';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('AlignmentViewerTab');

    export default Vue.extend({
        name: 'AlignmentViewerTab',
        components: {
            ToolCitationInfo,
            AlignmentViewer,
        },
        props: {
            job: {
                type: Object as () => Job,
                required: true,
            },
            tool: {
                type: Object as () => Tool,
                required: true,
            },
        },
        data() {
            return {
                loading: false,
                format: 'fasta',
            };
        },
        beforeDestroy() {
            EventBus.$off('fullscreen');
            EventBus.$off('tool-tab-activated');
        },
        mounted() {
            EventBus.$on('fullscreen', (fullScreen: boolean) => {
                EventBus.$emit('alignment-viewer-resize', fullScreen);
            });
            EventBus.$on('tool-tab-activated', (jobView: string) => {
                if (jobView === 'alignmentViewer') {
                    EventBus.$emit('alignment-viewer-resize', false);
                }
            });
            if (!this.job.alignments) {
                this.loading = true;
                this.$store.dispatch('jobs/loadJobAlignments', this.job.jobID)
                    .catch((e: any) => {
                        logger.error(e);
                    })
                    .finally(() => {
                        this.loading = false;
                    });
            }
        },
    });
</script>

<style lang="scss" scoped>

</style>
