<template>
    <div>
        <b-form-group class="textarea-group">
            <b-form-textarea class="alignment-viewer-alignment"
                             :placeholder="parameter.inputPlaceholder"
                             v-model="input"
                             cols="70"
                             spellcheck="false">
            </b-form-textarea>
            <b-button-group size="sm"
                            class="mt-1 mb-3">
                <b-btn variant="link"
                       @click="handlePasteExample">
                    <loading v-if="$store.state.loading.alignmentTextarea"
                             :size="20"/>
                    <span v-else
                          v-text="$t('tools.parameters.textArea.pasteExample')"></span>
                </b-btn>
            </b-button-group>
        </b-form-group>
        <b-btn class="submit-button float-right"
               @click="showAlignment"
               variant="primary">
            View Alignment
        </b-btn>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {FrontendToolParameter} from '@/types/toolkit/tools';
    import {Reformat} from '@/modules/reformat';
    import EventBus from '@/util/EventBus';
    import Logger from 'js-logger';
    import {sampleSeqService} from '@/services/SampleSeqService';
    import Loading from '@/components/utils/Loading.vue';

    const logger = Logger.get('AlignmentViewerView');

    export default Vue.extend({
        name: 'AlignmentViewerView',
        props: {
            parameter: Object as () => FrontendToolParameter,
        },
        components: {
            Loading,
        },
        data() {
            return {
                input: '',
            };
        },
        computed: {
            reformat(): Reformat {
                return new Reformat(this.input);
            },
            detectedFormat(): string {
                return this.reformat.getFormat();
            },
        },
        mounted() {
            EventBus.$on('forward-data', this.acceptForwardData);
        },
        beforeDestroy() {
            EventBus.$off('forward-data', this.acceptForwardData);
        },
        methods: {
            acceptForwardData(data: string): void {
                this.input = data;
            },
            handlePasteExample() {
                this.$store.commit('startLoading', 'alignmentTextarea');
                sampleSeqService.fetchSampleSequence(this.parameter.sampleInput)
                    .then((res: string) => {
                        this.input = res;
                    })
                    .catch((err: any) => {
                        logger.error('error when fetching sample sequence', err);
                        this.input = 'Error!';
                    })
                    .finally(() => {
                        this.$store.commit('stopLoading', 'alignmentTextarea');
                    });
            },
            showAlignment() {
                EventBus.$emit('alignment-viewer-result-open', {
                    sequences: this.input,
                    format: this.detectedFormat.toLowerCase(),
                });
            },
        },
    });
</script>

<style lang="scss" scoped>
    .alignment-viewer-alignment {
        height: 20em;
    }
</style>
