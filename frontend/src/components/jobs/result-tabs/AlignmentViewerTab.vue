<template>
    <alignment-viewer :sequences="alignments"/>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Job} from '@/types/toolkit/jobs';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
    import {Tool} from '@/types/toolkit/tools';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';
    import {AlignmentItem} from '@/types/toolkit/results';
    import {resultsService} from '@/services/ResultsService';

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
            },
        },
        data() {
            return {
                alignments: undefined as AlignmentItem[] | undefined,
                loading: false,
            };
        },
        async created() {
            if (!this.alignments) {
                this.loading = true;
                try {
                    this.alignments = await resultsService.fetchAlignmentResults(this.job.jobID);
                } catch (e) {
                    logger.error(e);
                }
                this.loading = false;
            }
        },
        watch: {
            fullScreen: {
                immediate: true,
                handler(value: boolean): void {
                    EventBus.$emit('alignment-viewer-resize', value);
                },
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
