<template>
    <div>
        <h5>Protein ID: {{accession}}</h5>

        <div v-if="results && results.signal && results.signal.seq === 'yes'">
            <br>
            <span class="note"><b> We have detected a potential signal peptide in your query protein!</b></span>
        </div>

        <br> <br> <br>

        <table class="unstriped" id="resultTableQ2D">
            <tbody class="alignmentTBody">
            @Html(Common.quick2dWrapped(result, 0, 90))
            </tbody>
        </table>
        <hr class="horizontal-line">
        <br>

        <div class="text-center mb-5">
            SS = <span class="ss_h_b">&nbsp;&alpha;-helix&nbsp;</span><span
                class="ss_e_b">&nbsp;&beta;-strand&nbsp;</span><span class="ss_pihelix">&nbsp;&pi;-helix&nbsp;</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CC
            = <span class="CC_b">Coiled Coils</span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TM = <span class="CC_m">Transmembrane</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DO
            = <span class="CC_do">Disorder</span>
        </div>

        <tool-citation-info :tool="tool"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Loading from '@/components/utils/Loading.vue';
    import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
    import {Tool} from '@/types/toolkit/tools';
    import {Job} from '@/types/toolkit/jobs';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('Quick2DResultsTab');

    export default Vue.extend({
        name: 'Quick2DResultsTab',
        components: {
            ToolCitationInfo,
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
                // TODO type this or put into job
                results: undefined as any,
                loading: false,
            };
        },
        computed: {
            accession(): string {
                if (!this.results) {
                    return '';
                }
                return this.results.query[0][0].slice(50);
            },
        },
        mounted() {
            this.loading = true;
            resultsService.fetchResults(this.job.jobID)
                .then((results: any) => {
                    this.results = results;
                })
                .catch((e: any) => {
                    logger.error(e);
                })
                .finally(() => {
                    this.loading = false;
                });
        },
    });
</script>

<style lang="scss" scoped>

</style>
