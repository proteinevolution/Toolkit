<template>
      <div ref="visualization" class="result-section">
        <div v-html="$t('jobs.results.hmmer.numHits', { num: total })"></div>
        <h4>{{ $t('jobs.results.hitlist.vis') }}</h4>
        <hit-map :job="job" @elem-clicked="scrollToElem" @resubmit-section="resubmitSection" />
      </div>
</template>

<script lang="ts">
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import {
  PLMBLASTAlignmentItem,
  PLMBLASTHHInfoResult,
  SearchAlignmentItemRender,
} from '@/types/toolkit/results';
import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';

export default SearchResultTabMixin.extend({
  name: 'PLMBlastResultsTab',
  components: {
    Loading,
    HitListTable,
    HitMap,
    IntersectionObserver,
  },
  data() {
    return {
      alignments: undefined as PLMBLASTAlignmentItem[] | undefined,
      info: undefined as PLMBLASTHHInfoResult | undefined,
      breakAfter: 90,
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
  methods: {

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
