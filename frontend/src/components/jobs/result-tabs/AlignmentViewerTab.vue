<template>
    <div>
        <alignment-viewer :sequences="job.alignments"/>

        <tool-citation-info :tool="tool"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Job} from '@/types/toolkit/jobs';
    import Loading from '@/components/utils/Loading.vue';
    import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
    import {Tool} from '@/types/toolkit/tools';
    import Logger from 'js-logger';

    const logger = Logger.get('AlignmentViewerTab');

    export default Vue.extend({
        name: 'AlignmentViewerTab',
        components: {
            ToolCitationInfo,
            AlignmentViewer,
            Loading,
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
        mounted() {
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
