<template>
    <Loading :message="$t('loading')"
             v-if="loading || !results"/>
    <div v-else
         class="font-small">
        <b v-if="results.results.hits.length === 0"
           v-text="$t('jobs.results.hhomp.noResults')">
        </b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{$t('jobs.results.hitlist.visLink')}}</a>
                <a @click="scrollTo('hits')">{{$t('jobs.results.hitlist.hitsLink')}}</a>
                <a @click="scrollTo('alignments')"
                   class="mr-4">{{$t('jobs.results.hitlist.alnLink')}}</a>
                <a class="border-right mr-4"></a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.forwardQueryA3M')}}</a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.hhomp.numHits', {num: results.results.hits.length})"></div>
            <div v-html="$t('jobs.results.hhomp.ompProb', {prob: results.results.overallprob})"></div>

            <div class="result-section"
                 ref="visualization">
                <h5>{{$t('jobs.results.hitlist.vis')}}</h5>
                <div class="huge"></div>
            </div>

            <div class="result-section"
                 ref="hits">
                <h5>{{$t('jobs.results.hitlist.hits')}}</h5>
                <div class="huge"></div>
            </div>

            <div class="result-section"
                 ref="alignments">
                <h5>{{$t('jobs.results.hitlist.aln')}}</h5>
                <div class="huge"></div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('HHompResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'HHompResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                color: false,
                wrap: true,
                // TODO type results
                results: undefined as any,
            };
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
            },
            scrollTo(ref: string): void {
                if (this.$refs[ref]) {
                    (this.$refs[ref] as HTMLElement).scrollIntoView({
                        block: 'start',
                        behavior: 'smooth',
                    });
                }
            },
            forwardQuery(): void {
                alert('implement me!');
            },
            toggleColor(): void {
                this.color = !this.color;
            },
            toggleWrap(): void {
                this.wrap = !this.wrap;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .huge {
        height: 500px;
    }

    .result-section {
        padding-top: 3.5rem;
    }
</style>
