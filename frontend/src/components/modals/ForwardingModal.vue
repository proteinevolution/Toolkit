<template>
    <BaseModal :title="$t('jobs.results.actions.forward')"
               id="forwardingModal"
               :size="largeModal ? 'lmd' : 'sm'"
               @hidden="onHidden"
               @shown="onShown">
        <b-row>
            <b-col cols="12"
                   v-if="forwardingApiOptions"
                   class="mb-4">
                <div class="bg-secondary rounded p-4">
                    <b-row>
                        <b-col>
                            <b>Forward hits</b>
                            <b-form-radio v-model="forwardHitsMode"
                                          :value="ForwardHitsMode.SELECTED"
                                          :disabled="!selectModePossible"
                                          class="mt-4">
                                Selected
                            </b-form-radio>
                            <b-form-radio v-model="forwardHitsMode"
                                          :value="ForwardHitsMode.EVALUE"
                                          class="mt-3">
                                E-value better than
                                <b-form-input v-model="evalThreshold"
                                              placeholder="E-value threshold"
                                              class="mt-1"
                                              size="sm"
                                              @focus="forwardHitsMode = ForwardHitsMode.EVALUE"/>
                            </b-form-radio>

                        </b-col>
                        <b-col v-if="largeModal">
                            <b>Select sequence length</b>
                            <b-form-radio v-model="sequenceLengthMode"
                                          :value="SequenceLengthMode.ALN"
                                          class="mt-4">
                                Aligned regions
                            </b-form-radio>
                            <b-form-radio v-model="sequenceLengthMode"
                                          :value="SequenceLengthMode.FULL"
                                          class="mt-3">
                                Full length sequences
                            </b-form-radio>
                        </b-col>
                    </b-row>
                </div>
            </b-col>
            <b-col cols="12"
                   :md="largeModal ? '4': '12'">
                <b-form-select v-model="selectedTool"
                               :options="toolOptions"
                               value-field="name"
                               text-field="longname"
                               :disabled="!forwardingData && !forwardingApiOptions">
                    <template slot="first">
                        <option :value="null"
                                v-text="$t('jobs.forwarding.' + ((forwardingData || forwardingApiOptions) ? 'selectPlaceholder' : 'noData'))"></option>
                    </template>
                </b-form-select>
            </b-col>
            <b-col cols="12"
                   :md="largeModal ? '8': '12'">
                <b-button variant="primary"
                          @click="forward"
                          class="mt-3 float-right"
                          :class="{'mt-md-0': largeModal}"
                          :disabled="forwardingDisabled">
                    <Loading v-if="loading"
                             :size="16"
                             :message="$t('loading')"/>
                    <span v-else
                          v-text="$t('jobs.results.actions.forward')"></span>
                </b-button>
            </b-col>
        </b-row>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {ForwardingApiOptions, ForwardingMode, Tool} from '@/types/toolkit/tools';
    import EventBus from '@/util/EventBus';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import Loading from '@/components/utils/Loading.vue';
    import {ForwardHitsMode, SequenceLengthMode} from '@/types/toolkit/enums';

    const logger = Logger.get('ForwardingModal');

    export default Vue.extend({
        name: 'ForwardingModal',
        components: {
            BaseModal,
            Loading,
        },
        props: {
            forwardingMode: {
                type: Object as () => ForwardingMode,
                required: true,
            },
            forwardingApiOptions: {
                type: Object as () => ForwardingApiOptions,
                required: false,
            },
            forwardingData: {
                type: String,
                required: false,
            },
            forwardingJobID: {
                type: String,
                required: true,
            },
        },
        data() {
            return {
                selectedTool: undefined as string | undefined,
                forwardHitsMode: ForwardHitsMode.SELECTED,
                sequenceLengthMode: SequenceLengthMode.ALN,
                evalThreshold: 0.001 as number | string,
                internalForwardData: '',
                loading: false,
                SequenceLengthMode,
                ForwardHitsMode,
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            toolOptions(): Tool[] {
                const alignmentOptions: string[] = this.sequenceLengthMode === SequenceLengthMode.ALN ?
                    this.forwardingMode.alignment : this.forwardingMode.multiSeq;
                if (alignmentOptions) {
                    return this.tools
                        .filter((t: Tool) => alignmentOptions.includes(t.name))
                        .sort((t1: Tool, t2: Tool) => {
                            const t1Name = t1.longname.toLowerCase();
                            const t2Name = t2.longname.toLowerCase();
                            if (t1Name < t2Name) { // sort string ascending
                                return -1;
                            } else if (t1Name > t2Name) {
                                return 1;
                            }
                            return 0;
                        });
                } else {
                    return [];
                }
            },
            forwardingDisabled(): boolean {
                return this.loading || !this.selectedTool
                    || (!this.forwardingData && !this.forwardingApiOptions)
                    || (this.forwardingApiOptions && this.forwardHitsMode === ForwardHitsMode.SELECTED
                        && this.forwardingApiOptions.selectedItems.length < 1)
                    || (this.forwardingApiOptions && this.forwardHitsMode === ForwardHitsMode.EVALUE
                        && isNaN(parseFloat(this.evalThreshold.toString())));
            },
            selectModePossible(): boolean {
                return this.forwardingApiOptions && this.forwardingApiOptions.selectedItems.length > 0;
            },
            largeModal(): boolean {
                return this.forwardingApiOptions && !this.forwardingApiOptions.disableSequenceLengthSelect;
            },
        },
        watch: {
            sequenceLengthMode(value: SequenceLengthMode): void {
                this.presetSelectedTool(value);
            },
        },
        methods: {
            async forward() {
                if (this.selectedTool) {
                    this.loading = true;
                    this.internalForwardData = this.forwardingData;

                    if (this.forwardingApiOptions) {
                        try {
                            this.internalForwardData = await resultsService.generateForwardingData(this.forwardingJobID,
                                {
                                    'forwardHitsMode': this.forwardHitsMode,
                                    'sequenceLengthMode': this.sequenceLengthMode,
                                    'eval': this.evalThreshold,
                                    'selected': this.forwardHitsMode === ForwardHitsMode.SELECTED ?
                                        this.forwardingApiOptions.selectedItems : [],
                                });

                        } catch (e) {
                            logger.error(e);
                            this.$alert(this.$t('errors.couldNotLoadForwardData'), 'danger');
                            this.loading = false;
                            return;
                        }
                    }
                    this.$router.push('/tools/' + this.selectedTool, () => {
                        EventBus.$on('paste-area-loaded', this.pasteForwardData);
                    });
                    EventBus.$emit('hide-modal', 'forwardingModal');
                    this.loading = false;
                    this.resetData();
                } else {
                    logger.log('no tool selected');
                }
            },
            pasteForwardData() {
                EventBus.$off('paste-area-loaded', this.pasteForwardData);
                logger.log(this.internalForwardData);
                EventBus.$emit('forward-data', {data: this.internalForwardData, jobID: this.forwardingJobID});
            },
            onShown(): void {
                if (this.forwardingApiOptions) {
                    if (this.forwardingApiOptions.selectedItems.length === 0) {
                        this.forwardHitsMode = ForwardHitsMode.EVALUE;
                    } else {
                        this.forwardHitsMode = ForwardHitsMode.SELECTED;
                    }
                }
                this.presetSelectedTool(this.sequenceLengthMode);
            },
            presetSelectedTool(sequenceLengthMode: SequenceLengthMode): void {
                if (sequenceLengthMode && this.forwardingMode.alignment) {
                    this.selectedTool = sequenceLengthMode === SequenceLengthMode.ALN ?
                        this.forwardingMode.alignment[0] : this.forwardingMode.multiSeq[0];
                }
            },
            onHidden(): void {
                this.resetData();
                this.$emit('hidden');
            },
            resetData() {
                // reset data
                this.forwardHitsMode = ForwardHitsMode.SELECTED;
                this.sequenceLengthMode = SequenceLengthMode.ALN;
                this.selectedTool = undefined;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .warning-alert {
        display: inline-block;
        margin-bottom: 0;
        padding: 0.4rem 1rem;
    }
</style>
