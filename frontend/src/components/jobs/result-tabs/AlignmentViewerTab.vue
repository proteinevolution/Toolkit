<template>
    <alignment-viewer :sequences="alignments"/>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';
    import {AlignmentItem} from '@/types/toolkit/results';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('AlignmentViewerTab');

    export default mixins(ResultTabMixin).extend({
        name: 'AlignmentViewerTab',
        components: {
            AlignmentViewer,
        },
        data() {
            return {
                alignments: undefined as AlignmentItem[] | undefined,
                loading: false,
            };
        },
        methods: {
            async init() {
                this.alignments = await resultsService.fetchAlignmentResults(this.job.jobID);
            },
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
