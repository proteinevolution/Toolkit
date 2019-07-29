<template>
    <Loading :message="$t('loading')"
             v-if="!hitMap"/>
    <div v-else
         class="px-5">
        <div class="px-2 mb-3 d-flex flex-column">
            <b-btn variant="secondary"
                   class="ml-auto"
                   v-text="$t('jobs.results.hitlist.resubmitSection')"
                   @click="resubmitSection"
                   size="sm"/>
            <div class="mt-3 px-2">
                <vue-slider ref="slider"
                            v-model="resubmitSelection"
                            :min="1"
                            :max="hitMap.queryLength"
                            :enable-cross="false"/>
            </div>
        </div>
        <div class="hit-map-container">
            <map name="hitMap">
                <area v-for="area in hitAreas"
                      :key="'area' + area.num"
                      alt=""
                      shape="rect"
                      :coords="coords(area)"
                      @click="$emit('elem-clicked', area.num)"
                      @mouseenter="onMouseEnter(area)"
                      @mouseleave="onMouseLeave(area)"/>
            </map>
            <div class="tooltip bs-tooltip-bottom"
                 :class="[hoverElem !== undefined ? 'show' : '']"
                 :style="toolTipPosition">
                <div class="arrow"></div>
                <div class="tooltip-inner"
                     v-text="toolTipText">
                </div>
            </div>
            <img :src="hitMapImgPath"
                 alt="Hits"
                 ref="blastMapImg"
                 usemap="#hitMap">
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {resultsService} from '@/services/ResultsService';
    import {Job} from '@/types/toolkit/jobs';
    import {HitMapItem, HitMapResponse} from '@/types/toolkit/results';
    import VueSlider from 'vue-slider-component';
    import Loading from '@/components/utils/Loading.vue';

    export default Vue.extend({
        name: 'HitMap',
        components: {
            VueSlider,
            Loading,
        },
        props: {
            job: {
                type: Object as () => Job,
                required: true,
            },
        },
        data() {
            return {
                resubmitSelection: [1, 1],
                hitMap: undefined as HitMapResponse | undefined,
                hoverElem: undefined as HitMapItem | undefined,
                toolTipPosition: {
                    top: '0px',
                    left: '0px',
                    right: '0px',
                    width: '0px',
                },
                toolTipText: '',
            };
        },
        computed: {
            hitMapImgPath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, this.job.jobID + '.png');
            },
            hitAreas(): HitMapItem[] | undefined {
                if (!this.hitMap) {
                    return undefined;
                }
                return this.hitMap.hitAreas;
            },
        },
        async mounted() {
            this.hitMap = await resultsService.getFile(this.job.jobID, 'hitmap.json') as HitMapResponse;
            this.$nextTick(() => {
                if (!this.hitMap) {
                    return;
                }
                (this.$refs.slider as any).setValue([this.hitMap.resubmitStart, this.hitMap.resubmitEnd]);
            });
        },
        methods: {
            resubmitSection(): void {
                alert('implement me!');
            },
            coords(area: HitMapItem): string {
                return `${area.l},${area.t},${area.r},${area.b}`;
            },
            onMouseEnter(area: HitMapItem): void {
                this.hoverElem = area;
                this.toolTipPosition = {
                    top: (area.b + 2) + 'px',
                    left: (area.l) + 'px',
                    right: (area.r) + 'px',
                    width: (area.r - area.l) + 'px',
                };
                this.toolTipText = area.title;
            },
            onMouseLeave(area: HitMapItem): void {
                if (this.hoverElem === area) {
                    this.hoverElem = undefined;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .hit-map-container {
        position: relative;

        .tooltip {
            display: flex;
            justify-content: center;
            pointer-events: none;
            transition: opacity 0.2s ease-in-out;

            .arrow {
                left: 50%;
                transform: translateX(-50%);
            }

            .tooltip-inner {
                max-width: 90%;
            }
        }
    }
</style>
