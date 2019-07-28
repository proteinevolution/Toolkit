<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.psiblast.noResults')">
        </b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{$t('jobs.results.hitlist.visLink')}}</a>
                <a @click="scrollTo('hits')">{{$t('jobs.results.hitlist.hitsLink')}}</a>
                <a @click="scrollTo('alignments')"
                   class="mr-4">{{$t('jobs.results.hitlist.alnLink')}}</a>
                <a class="border-right mr-4"></a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.selectAll')}}</a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.forward')}}</a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.downloadMSA')}}</a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.psiblast.numHits', {num: total})"></div>
            <div class="result-section"
                 ref="visualization">
                <h4>{{$t('jobs.results.hitlist.vis')}}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"/>
            </div>

            <div class="result-section"
                 ref="hits">
                <h4 class="mb-4">{{$t('jobs.results.hitlist.hits')}}</h4>
                <hit-list-table :job="job"
                                :fields="hitListFields"
                                @elem-clicked="scrollToElem"/>
            </div>
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
    import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';

    const logger = Logger.get('PsiblastResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'PsiblastResultsTab',
        components: {
            Loading,
            HitListTable,
            HitMap,
            IntersectionObserver,
        },
        data() {
            return {
                total: 100,
                hitListFields: [{
                    key: 'num',
                    label: this.$t('jobs.results.psiblast.table.num'),
                    sortable: true,
                }, {
                    key: 'accession',
                    label: this.$t('jobs.results.psiblast.table.accession'),
                    sortable: true,
                }, {
                    key: 'description',
                    label: this.$t('jobs.results.psiblast.table.description'),
                    sortable: true,
                }, {
                    key: 'eValue',
                    label: this.$t('jobs.results.psiblast.table.eValue'),
                    sortable: true,
                }, {
                    key: 'bitscore',
                    label: this.$t('jobs.results.psiblast.table.bitscore'),
                    sortable: true,
                }, {
                    key: 'ref_len',
                    label: this.$t('jobs.results.psiblast.table.ref_len'),
                    sortable: true,
                }, {
                    key: 'hit_len',
                    label: this.$t('jobs.results.psiblast.table.hit_len'),
                    sortable: true,
                }],
            };
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

    .alignments-table {
        font-size: 0.95em;

        .blank-row {
            height: 2rem;
        }

        .sequence td {
            word-break: keep-all;
            white-space: nowrap;
            font-family: $font-family-monospace;
            padding: 0 1rem 0 0;
        }

        a {
            cursor: pointer;
            color: $primary;

            &:hover {
                color: $tk-dark-green;
            }
        }
    }
</style>
