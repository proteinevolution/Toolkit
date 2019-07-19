<template>
    <alignment-viewer :sequences="job.alignments"/>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Job} from '@/types/toolkit/jobs';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
    import {Tool} from '@/types/toolkit/tools';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('AlignmentViewerTab');

    export default Vue.extend({
        name: 'AlignmentViewerTab',
        components: {
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
            fullScreen: {
                type: Boolean,
                required: false,
                default: false,
            }
        },
        data() {
            return {
                loading: false,
                format: 'fasta',
            };
        },
        beforeDestroy() {
            EventBus.$off('fullscreen');
        },
        mounted() {
            EventBus.$emit('alignment-viewer-resize', this.fullScreen);
            EventBus.$on('fullscreen', (fullScreen: boolean) => {
                EventBus.$emit('alignment-viewer-resize', fullScreen);
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
