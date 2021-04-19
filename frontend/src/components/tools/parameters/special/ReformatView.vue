<template>
    <div>
        <b-form-textarea v-model="input"
                         class="textarea-input break-all"
                         :placeholder="$t('tools.inputPlaceholder.' + parameter.placeholderKey)"
                         cols="70"
                         spellcheck="false"
                         @input="clearOutput" />
        <b-button-group size="sm"
                        class="mt-1 mb-3">
            <b-btn variant="link"
                   @click="handlePasteExample">
                <loading v-if="$store.state.loading.alignmentTextarea"
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
        <b-row align-h="center"
               class="my-2">
            <b-col cols="12"
                   md="4">
                <multiselect v-model="selectedOutputFormat"
                             :allow-empty="true"
                             :options="outputFormatOptions"
                             :disabled="!detectedFormat"
                             track-by="value"
                             label="text"
                             :placeholder="$t('tools.reformat.selectOutputFormat')"
                             :searchable="false"
                             select-label=""
                             deselect-label=""
                             selected-label=""
                             @select="computeOutput" />
            </b-col>
        </b-row>
        <div v-if="output">
            <b-form-textarea v-model="output"
                             class="textarea-output break-all"
                             cols="70"
                             spellcheck="false"
                             readonly />
            <div class="halign-center-wrapper mt-2">
                <b-button-group class="mt-2 output-button-group">
                    <b-dropdown :text="$t('tools.reformat.forwardTo')"
                                variant="primary">
                        <b-dropdown-item v-for="option in forwardingOptions"
                                         :key="option.value"
                                         @click="forward(option)">
                            {{ option.text }}
                        </b-dropdown-item>
                    </b-dropdown>
                    <b-button variant="primary"
                              @click="copyToClipboard">
                        {{ $t('tools.reformat.copyToClipboard') }}
                    </b-button>
                    <b-button download="reformat_download.txt"
                              :href="'data:application/octet-stream;content-disposition:attachment;filename=file.txt;charset=utf-8,'
                                  + encodeURIComponent(output)"
                              variant="primary">
                        {{ $t('tools.reformat.download') }}
                    </b-button>
                </b-button-group>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {FrontendToolParameter, SelectOption, SequenceValidationParams, Tool} from '@/types/toolkit/tools';
import {Reformat} from '@/modules/reformat';
import Multiselect from 'vue-multiselect';
import {AlignmentSeqFormat} from '@/types/toolkit/enums';
import EventBus from '@/util/EventBus';
import Logger from 'js-logger';
import {sampleSeqService} from '@/services/SampleSeqService';
import Loading from '@/components/utils/Loading.vue';
import {jobService} from '@/services/JobService';

const logger = Logger.get('ReformatView');

export default Vue.extend({
    name: 'ReformatView',
    components: {
        Multiselect,
        Loading,
    },
    props: {
        parameter: {
            type: Object as () => FrontendToolParameter,
            default: undefined,
        },
    },
    data() {
        return {
            input: '',
            output: '',
            outputFormatOptions: [
                {value: AlignmentSeqFormat.FASTA, text: 'FASTA', $isDisabled: false},
                {value: AlignmentSeqFormat.CLUSTAL, text: 'CLUSTAL', $isDisabled: false},
                {value: AlignmentSeqFormat.STOCKHOM, text: 'STOCKHOLM', $isDisabled: false},
            ],
            forwardingOptions: [] as SelectOption[],
            selectedOutputFormat: undefined,
            selectedForwardingTool: undefined,
        };
    },
    computed: {
        tools(): Tool[] {
            return this.$store.getters['tools/tools'];
        },
        reformat(): Reformat {
            return new Reformat(this.input);
        },
        detectedFormat(): string {
            return this.reformat.getFormat();
        },
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
        handlePasteExample(): void {
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
        computeOutput(selectedFormat: SelectOption): void {
            if (selectedFormat !== undefined && this.reformat !== undefined) {
                jobService.logFrontendJob(this.$route.params.toolName);
                this.output = this.reformat.reformat(selectedFormat.value);
                this.forwardingOptions = this.tools
                    .filter((tool: Tool) => {
                        if (!tool.validationParams) {
                            return false;
                        }
                        const allowedFormats = (tool.validationParams as SequenceValidationParams).allowedSeqFormats;
                        const maxNumSeqs = (tool.validationParams as SequenceValidationParams).maxNumSeq;
                        const sameLength = (tool.validationParams as SequenceValidationParams).requiresSameLengthSeq;

                        return allowedFormats !== undefined &&
                            allowedFormats.includes((selectedFormat.value as AlignmentSeqFormat)) &&
                            (maxNumSeqs && maxNumSeqs > 1) && (sameLength);
                    })
                    .sort((t1: Tool, t2: Tool) => {
                        const t1Name = t1.longname.toLowerCase();
                        const t2Name = t2.longname.toLowerCase();
                        if (t1Name < t2Name) { // sort string ascending
                            return -1;
                        } else if (t1Name > t2Name) {
                            return 1;
                        }
                        return 0;
                    })
                    .map((tool: Tool) => ({
                        value: tool.name,
                        text: tool.longname,
                    }));
            }
        },
        clearOutput(): void {
            this.selectedOutputFormat = undefined;
            this.output = '';
        },
        forward(selectedTool: SelectOption): void {
            this.$router.push({name: 'tools', params: {toolName: selectedTool.value, input: this.output}});
        },
        copyToClipboard() {
            (this as any).$copyText(this.output).then(() => {
                this.$alert(this.$t('tools.reformat.copySuccess'));
            }, () => {
                this.$alert(this.$t('tools.reformat.copyFailure'));
            });
        },
    },
});
</script>

<style lang="scss" scoped>
.textarea-input, .textarea-output {
  background-color: $white;
  font-family: $font-family-monospace;
  font-size: 0.9em;
  height: 15em;
}

.btn-link:hover, .btn-link:active, .btn-link:focus {
  text-decoration: none;
}

.validation-alert {
  margin-top: 0.5rem;
  float: right;
  padding: 0.4rem 0.5rem;
}

.halign-center-wrapper {
  text-align: center;
}

.output-button-group {
  display: inline-block;
}
</style>
