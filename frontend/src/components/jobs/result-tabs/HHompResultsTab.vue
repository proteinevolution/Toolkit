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

                <div class="px-5">
                    <div class="px-2 d-flex flex-column">
                        <b-btn variant="secondary"
                               class="ml-auto"
                               v-text="$t('jobs.results.hitlist.resubmitSection')"
                               size="sm"/>
                        <div class="mt-3 px-2">
                            <vue-slider v-model="resubmitSelection"
                                        :min="1"
                                        :max="sequenceLength"
                                        :enable-cross="false"/>
                        </div>
                    </div>

                    <div class="blast-map-container">
                        <map name="blastMap">
                            <area v-for="(al, i) in results.results.alignments"
                                  :key="i"
                                  alt=""
                                  shape="rect"
                                  :coords="areaStart(al) + ',' + (28 + i * 18) + ',' + areaEnd(al) + ',' + (38 + i * 18)"
                                  @click="scrollToElem(al.no)"
                                  @mouseenter="onMouseEnter(al)"
                                  @mouseleave="onMouseLeave(al)"/>
                        </map>
                        <div class="tooltip bs-tooltip-bottom"
                             :class="[hoverElem !== undefined ? 'show' : '']"
                             :style="{top: toolTipTop + 'px'}">
                            <div class="arrow"></div>
                            <div class="tooltip-inner"
                                 v-text="toolTipText">
                            </div>
                        </div>
                        <img :src="blastMapImgPath"
                             alt="Blast Hits"
                             ref="blastMapImg"
                             usemap="#blastMap">
                    </div>
                </div>
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
    import VueSlider from 'vue-slider-component';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('HHompResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'HHompResultsTab',
        components: {
            Loading,
            VueSlider,
        },
        data() {
            return {
                resubmitSelection: [1, 1],
                color: false,
                wrap: true,
                // TODO type results
                results: undefined as any,
                hoverElem: undefined as any,
                toolTipTop: 0,
                toolTipText: '',
            };
        },
        computed: {
            blastMapImgPath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, this.job.jobID + '.png');
            },
            sequenceLength(): number {
                return this.results.query[0][1].length;
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
                this.resubmitSelection[0] = this.results.results.alignments[0].query.start;
                this.resubmitSelection[1] = this.results.results.alignments[0].query.end;
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
                alert('implement me!');
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
            areaTitle(al: any): string {
                if (!al) {
                    return '';
                }
                return `Prob=${al.info.probab_OMP}% E=${al.info.eval}  ${al.template.name} ${al.header}`;
            },
            areaStart(al: any): number {
                return 12;
            },
            areaEnd(al: any): number {
                return 800 - 12;
            },
            onMouseEnter(al: any): void {
                this.hoverElem = al;
                this.toolTipTop = al.no * 18 + 20;
                this.toolTipText = this.areaTitle(al);
            },
            onMouseLeave(al: any): void {
                if (this.hoverElem === al) {
                    this.hoverElem = undefined;
                }
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

    .blast-map-container {
        position: relative;

        .tooltip {
            width: 100%;
            display: flex;
            justify-content: center;
            pointer-events: none;
            transition: opacity 0.2s ease-in-out;

            .arrow {
                left: 50%;
            }

            .tooltip-inner {
                max-width: 80%;
            }
        }
    }
</style>
