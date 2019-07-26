<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
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

            <div v-html="$t('jobs.results.hhomp.numHits', {num: total})"></div>

            <div class="result-section"
                 ref="visualization">
                <h4>{{$t('jobs.results.hitlist.vis')}}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"
                         @total-changed="totalChanged"/>
            </div>

            <div class="result-section"
                 ref="hits">
                <h4 class="mb-4">{{$t('jobs.results.hitlist.hits')}}</h4>
                <hit-list-table :job="job"
                                :fields="hitListFields"
                                @elem-clicked="scrollToElem"/>
            </div>

            <div class="result-section"
                 ref="alignments">
                <h4>{{$t('jobs.results.hitlist.aln')}}</h4>
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
    import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
    import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';

    const logger = Logger.get('HHompResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'HHompResultsTab',
        components: {
            Loading,
            HitListTable,
            HitMap,
        },
        data() {
            return {
                total: 100,
                color: false,
                wrap: true,
                hitListFields: [{
                    key: 'num',
                    label: this.$t('jobs.results.hhomp.table.num'),
                    sortable: true,
                }, {
                    key: 'acc',
                    label: this.$t('jobs.results.hhomp.table.hit'),
                    sortable: true,
                }, {
                    key: 'name',
                    label: this.$t('jobs.results.hhomp.table.name'),
                    sortable: true,
                }, {
                    key: 'probabHit',
                    label: this.$t('jobs.results.hhomp.table.probHits'),
                    sortable: true,
                }, {
                    key: 'probabOMP',
                    label: this.$t('jobs.results.hhomp.table.probOMP'),
                    sortable: true,
                }, {
                    key: 'eval',
                    label: this.$t('jobs.results.hhomp.table.eVal'),
                    sortable: true,
                }, {
                    key: 'ssScore',
                    label: this.$t('jobs.results.hhomp.table.ssScore'),
                    sortable: true,
                }, {
                    key: 'alignedCols',
                    label: this.$t('jobs.results.hhomp.table.cols'),
                    sortable: true,
                }, {
                    key: 'templateRef',
                    label: this.$t('jobs.results.hhomp.table.targetLength'),
                    sortable: true,
                }],
            };
        },
        methods: {
            async init() {
            },
            scrollTo(ref: string): void {
                if (this.$refs[ref]) {
                    (this.$refs[ref] as HTMLElement).scrollIntoView({
                        block: 'start',
                        behavior: 'smooth',
                    });
                }
            },
            scrollToElem(no: string): void {
                alert('implement me!' + no);
            },
            resubmitSection(): void {
                alert('implement me!');
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
            totalChanged(value: number): void {
                this.total = value;
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
