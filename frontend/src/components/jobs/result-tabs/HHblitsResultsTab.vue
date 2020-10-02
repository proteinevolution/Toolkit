<template>
    <Loading v-if="loading"
             :message="$t('loading')" />
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.hhblits.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ $t('jobs.results.hitlist.visLink') }}</a>
                <a @click="scrollTo('hits')">{{ $t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="mr-4"
                   @click="scrollTo('alignments')">{{ $t('jobs.results.hitlist.alnLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{active: allSelected}"
                   @click="toggleAllSelected">
                    {{ $t('jobs.results.actions.selectAll') }}</a>
                <a @click="forward(false)">{{ $t('jobs.results.actions.forward') }}</a>
                <a @click="forwardQueryA3M">{{ $t('jobs.results.actions.forwardQueryA3M') }}</a>
                <a :class="{active: color}"
                   @click="toggleColor">{{ $t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{active: wrap}"
                   @click="toggleWrap">{{ $t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="$t('jobs.results.hhblits.numHits', {num: info.num_hits})"></div>

            <div v-if="info.coil === '0' || info.tm > '0' || info.signal === '1'"
                 class="mt-2">
                {{ $t('jobs.results.sequenceFeatures.header') }}
                <b v-if="info.coil === '0'"
                   v-html="$t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '0'"
                   v-html="$t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'"
                   v-html="$t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div ref="visualization"
                 class="result-section">
                <h4>{{ $t('jobs.results.hitlist.vis') }}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"
                         @resubmit-section="resubmitSection" />
            </div>

            <div ref="hits"
                 class="result-section">
                <h4 class="mb-4">
                    {{ $t('jobs.results.hitlist.hits') }}
                </h4>
                <hit-list-table :job="job"
                                :fields="hitListFields"
                                :selected-items="selectedItems"
                                @elem-clicked="scrollToElem" />
            </div>

            <div ref="alignments"
                 class="result-section">
                <h4>{{ $t('jobs.results.hitlist.aln') }}</h4>

                <div ref="scrollElem"
                     class="table-responsive">
                    <table class="alignments-table">
                        <tbody>
                            <template v-for="(al, i) in alignments">
                                <tr :key="'alignment-' + al.num"
                                    :ref="'alignment-' + al.num"
                                    class="blank-row">
                                    <td colspan="4">
                                        <hr v-if="i !== 0">
                                    </td>
                                </tr>
                                <tr :key="'template-alignment-' + al.num">
                                    <td></td>
                                    <td colspan="3">
                                        <a @click="displayTemplateAlignment(al.template.accession)"
                                           v-text="$t('jobs.results.hhblits.templateAlignment')"></a>
                                    </td>
                                </tr>
                                <tr :key="'select-alignment-' + al.num"
                                    class="font-weight-bold">
                                    <td class="no-wrap">
                                        <b-checkbox class="d-inline"
                                                    :checked="selectedItems.includes(al.num)"
                                                    @change="check($event, al.num)" />
                                        <span v-text="al.num + '.'"></span>
                                    </td>
                                    <td colspan="3"
                                        v-html="al.acc + ' ' + al.name"></td>
                                </tr>
                                <tr :key="'alignment-info-' + al.num">
                                    <td></td>
                                    <td colspan="3"
                                        v-html="$t('jobs.results.hhblits.alignmentInfo', al)"></td>
                                </tr>

                                <template v-for="(alPart, pi) in wrapAlignments(al)">
                                    <tr :key="'alignment-part-' + i + '-' + pi"
                                        class="blank-row">
                                        <td></td>
                                    </tr>
                                    <tr v-if="alPart.query.seq"
                                        :key="'alignment-seq-' + i + '-' + pi"
                                        class="sequence">
                                        <td></td>
                                        <td>Q</td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="coloredSeq(alPart.query.seq) + alEndRef(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.query.consensus"
                                        :key="'alignment-consensus-' + i + '-' + pi"
                                        class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td v-html="alPart.query.consensus"></td>
                                    </tr>
                                    <tr v-if="alPart.agree"
                                        :key="'alignment-agree-' + i + '-' + pi"
                                        class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td class="consensus-agree"
                                            v-text="alPart.agree"></td>
                                    </tr>
                                    <tr v-if="alPart.template.consensus"
                                        :key="'alignment-tpl-consensus-' + i + '-' + pi"
                                        class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td v-html="alPart.template.consensus"></td>
                                    </tr>
                                    <tr v-if="alPart.template.seq"
                                        :key="'alignment-tpls-seq-' + i + '-' + pi"
                                        class="sequence">
                                        <td></td>
                                        <td>T</td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="coloredSeq(alPart.template.seq) + alEndRef(alPart.template)"></td>
                                    </tr>
                                    <tr :key="'alignment-br-' + i + '-' + pi"
                                        class="blank-row">
                                        <td></td>
                                    </tr>
                                </template>
                            </template>

                            <tr v-if="alignments.length !== total">
                                <td colspan="4">
                                    <Loading v-if="loadingMore"
                                             :message="$t('jobs.results.alignment.loadingHits')"
                                             justify="center"
                                             class="mt-4" />
                                    <intersection-observer @intersect="intersected" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import {HHblitsAlignmentItem, HHblitsHHInfoResult, SearchAlignmentItemRender} from '@/types/toolkit/results';
import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';

export default SearchResultTabMixin.extend({
  name: 'HHblitsResultsTab',
  components: {
    Loading,
    HitListTable,
    HitMap,
    IntersectionObserver,
  },
  data() {
    return {
      alignments: undefined as HHblitsAlignmentItem[] | undefined,
      info: undefined as HHblitsHHInfoResult | undefined,
      breakAfter: 85,
      hitListFields: [{
        key: 'numCheck',
        label: this.$t('jobs.results.hhblits.table.num'),
        sortable: true,
      }, {
        key: 'acc',
        label: this.$t('jobs.results.hhblits.table.hit'),
        sortable: true,
      }, {
        key: 'name',
        label: this.$t('jobs.results.hhblits.table.name'),
        sortable: true,
      }, {
        key: 'probab',
        label: this.$t('jobs.results.hhblits.table.probHits'),
        sortable: true,
      }, {
        key: 'eval',
        label: this.$t('jobs.results.hhblits.table.eVal'),
        class: 'no-wrap',
        sortable: true,
      }, {
        key: 'alignedCols',
        label: this.$t('jobs.results.hhblits.table.cols'),
        sortable: true,
      }, {
        key: 'templateRef',
        label: this.$t('jobs.results.hhblits.table.targetLength'),
        sortable: true,
      }],
    };
  },
  methods: {
    wrapAlignments(al: HHblitsAlignmentItem): SearchAlignmentItemRender[] {
      if (this.wrap) {
        const res: SearchAlignmentItemRender[] = [];
        let qStart: number = al.query.start;
        let tStart: number = al.template.start;
        for (let start = 0; start < al.query.seq.length; start += this.breakAfter) {
          const end: number = start + this.breakAfter;
          const qSeq: string = al.query.seq.slice(start, end);
          const tSeq: string = al.template.seq.slice(start, end);
          const qEnd: number = qStart + qSeq.length - (qSeq.match(/[-.]/g) || []).length - 1;
          const tEnd: number = tStart + tSeq.length - (tSeq.match(/[-.]/g) || []).length - 1;
          res.push({
            agree: al.agree.slice(start, end),
            query: {
              consensus: al.query.consensus.slice(start, end),
              end: qEnd,
              name: al.query.name,
              ref: al.query.ref,
              seq: qSeq,
              start: qStart,
            },
            template: {
              accession: al.template.accession,
              consensus: al.template.consensus.slice(start, end),
              end: tEnd,
              ref: al.template.ref,
              seq: tSeq,
              start: tStart,
            },
          });
          qStart = qEnd + 1;
          tStart = tEnd + 1;
        }
        return res;
      } else {
        return [al];
      }
    },
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

  .sequence {
    td {
      word-break: keep-all;
      white-space: nowrap;
      font-family: $font-family-monospace;
      padding: 0 1rem 0 0;
    }

    .consensus-agree {
      white-space: pre-wrap;
    }
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
