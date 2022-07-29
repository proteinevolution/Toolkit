<template>
    <div>
        <b-form-group class="textarea-group">
            <b-form-textarea v-model="input"
                             class="textarea-input break-all"
                             :placeholder="$t('tools.inputPlaceholder.' + parameter.placeholderKey)"
                             cols="70"
                             spellcheck="false" />
            <b-button-group size="sm"
                            class="mt-1 mb-3">
                <b-btn variant="link"
                       @click="handlePasteExample">
                    <loading v-if="rootStore.loading.alignmentTextarea"
                             :size="20" />
                    <span v-else
                          v-text="$t('tools.parameters.textArea.pasteExample')"></span>
                </b-btn>
            </b-button-group>
            <b-alert v-if="detectedFormat"
                     :show="true"
                     variant="success"
                     class="validation-alert mb-0"
                     v-html="$t('tools.reformat.detectedFormat', {format: detectedFormat})" />
            <b-alert v-if="!detectedFormat && input"
                     :show="true"
                     variant="danger"
                     class="validation-alert mb-0"
                     v-html="$t('tools.reformat.invalidFormat')" />
        </b-form-group>
        <b-btn class="submit-button float-right"
               :disabled="!detectedFormat"
               variant="primary"
               @click="showAlignment"
               v-text="$t('tools.alignmentViewer.viewAlignment')" />
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
import {jobService} from '@/services/JobService';
import {mapStores} from 'pinia';
import {useRootStore} from '@/stores/root';

const logger = Logger.get('AlignmentViewerView');

export default Vue.extend({
    name: 'AlignmentViewerView',
    components: {
        Loading,
    },
    props: {
        parameter: Object as () => FrontendToolParameter,
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
            if (this.input.replace(/\s/g, '') !== '') {
                return this.reformat.getFormat();
            }
            return '';
        },
        ...mapStores(useRootStore),
    },
    mounted() {
        EventBus.$on('forward-data', this.acceptForwardData);
        EventBus.$emit('paste-area-loaded');
    },
    beforeDestroy() {
        EventBus.$off('forward-data', this.acceptForwardData);
    },
    methods: {
        acceptForwardData({data}: { data: string }): void {
            this.input = data;
        },
        handlePasteExample() {
            this.rootStore.loading.alignmentTextarea = true;
            sampleSeqService.fetchSampleSequence(this.parameter.sampleInput)
                .then((res: string) => {
                    this.input = res;
                })
                .catch((err: any) => {
                    logger.error('error when fetching sample sequence', err);
                    this.input = 'Error!';
                })
                .finally(() => {
                    this.rootStore.loading.alignmentTextarea = false;
                });
        },
        showAlignment() {
            jobService.logFrontendJob(this.$route.params.toolName);
            this.input = new Reformat(this.input).reformat('FASTA');
            EventBus.$emit('alignment-viewer-result-open', {
                sequences: this.input,
                format: this.detectedFormat.toLowerCase(),
            });
        },
    },
});
</script>

<style lang="scss" scoped>
.textarea-input {
  background-color: $white;
  font-family: $font-family-monospace;
  font-size: 0.9em;
  height: 20em;
}

.validation-alert {
  margin-top: 0.5rem;
  float: right;
  padding: 0.4rem 0.5rem;
}

</style>
