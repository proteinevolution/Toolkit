<template>
    <Loading v-if="loading" :message="$t('loading')" />
    <div v-else class="font-small">
        <b v-if="total === 0" v-text="$t('jobs.results.hmmer.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ $t('jobs.results.hitlist.visLink') }}</a>
                <a class="mr-4" @click="scrollTo('hits')">{{ $t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{ active: allSelected }" @click="toggleAllSelected">
                    {{ $t('jobs.results.actions.selectAll') }}</a
                >
                <a @click="forward(false)">{{ $t('jobs.results.actions.forward') }}</a>
            </div>

            <div v-html="$t('jobs.results.hmmer.numHits', { num: total })"></div>

            <div ref="visualization" class="result-section">
                <h4>{{ $t('jobs.results.hitlist.vis') }}</h4>
                <hit-map :job="job" @elem-clicked="scrollToElem" @resubmit-section="resubmitSection" />
            </div>

            <div ref="hits" class="result-section">
                <h4 class="mb-4">
                    {{ $t('jobs.results.hitlist.hits') }}
                </h4>
                <hit-list-table
                    :job="job"
                    :fields="hitListFields"
                    :selected-items="selectedItems"
                    @elem-clicked="scrollToElem" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';

export default SearchResultTabMixin.extend({
    name: 'PLMBlastResultsTab',
    components: {
        Loading,
        HitListTable,
        HitMap,
    },
    data() {
        return {
            hitListFields: [
                {
                    key: 'numCheck',
                    label: this.$t('jobs.results.hmmer.table.num'),
                    sortable: true,
                },
                {
                    key: 'acc',
                    label: this.$t('jobs.results.hmmer.table.accession'),
                    sortable: true,
                },
                {
                    key: 'name',
                    label: this.$t('jobs.results.hmmer.table.description'),
                    sortable: true,
                },
                {
                    key: 'eval',
                    label: this.$t('jobs.results.hmmer.table.eValue'),
                    class: 'no-wrap',
                    sortable: true,
                },
                {
                    key: 'bitScore',
                    label: this.$t('jobs.results.hmmer.table.bitscore'),
                    sortable: true,
                },
                {
                    key: 'hitLen',
                    label: this.$t('jobs.results.hmmer.table.hit_len'),
                    sortable: true,
                },
            ],
        };
    },
});
</script>

<style lang="scss" scoped>
.result-section {
    padding-top: 3.5rem;
}

.alignments-table {
    font-size: 0.95em;

    .blank-row {
        height: 0.8rem;
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
