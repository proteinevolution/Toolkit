<template>
    <div>
        <b-form-textarea class="textarea-input"
                         v-model="input"
                         @input="clearOutput"
                         :placeholder="$t('tools.reformat.inputPlaceholder')"
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
            <b-button variant="link"
                      :disabled="!detectedFormat"
                      @click="showAlignmentViewer">
                AlignmentViewer
            </b-button>
        </b-button-group>
        <b-alert show
                 v-if="detectedFormat"
                 variant="success"
                 class="validation-alert mb-0"
                 v-html="$t('tools.reformat.detectedFormat', {format: detectedFormat})">
        </b-alert>
        <b-row align-h="center" class="mb-3">
            <b-col cols="4">
                <multiselect v-model="selectedOutputFormat"
                             @select="computeOutput"
                             :allowEmpty="true"
                             :options="outputFormatOptions"
                             :disabled="!detectedFormat"
                             track-by="value"
                             label="text"
                             :placeholder="$t('tools.reformat.selectOutputFormat')"
                             :searchable="false"
                             selectLabel=""
                             deselectLabel=""
                             selectedLabel="">
                </multiselect>
            </b-col>
        </b-row>
        <div v-if="output">
            <b-form-textarea class="textarea-output"
                             v-model="output"
                             cols="70"
                             spellcheck="false"
                             readonly>
            </b-form-textarea>
            <div class="halign-center-wrapper mt-2">
                <b-button-group class="mt-2 output-button-group">
                    <b-dropdown :text="$t('tools.reformat.forwardTo')"
                                variant="primary">
                        <b-dropdown-item v-for="option in forwardingOptions"
                                         :key="option.value"
                                         @click="forward(option)">
                            {{option.text}}
                        </b-dropdown-item>
                    </b-dropdown>
                    <b-button @click="copyToClipboard"
                              variant="primary">
                        {{ $t('tools.reformat.copyToClipboard') }}
                    </b-button>
                    <b-button download="reformat_download.txt"
                              :href="'data:application/octet-stream;content-disposition:attachment;filename=file.txt;charset=utf-8,'
                                        + encodeURIComponent(this.output)"
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
    import Select from '../SelectParameter.vue';
    import {AlignmentSeqFormat} from '@/types/toolkit/enums';
    import EventBus from '@/util/EventBus';
    import Logger from 'js-logger';
    import {sampleSeqService} from '@/services/SampleSeqService';
    import Loading from '@/components/utils/Loading.vue';

    const logger = Logger.get('ReformatView');

    export default Vue.extend({
        name: 'ReformatView',
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
        components: {
            Multiselect,
            Select,
            Loading,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => FrontendToolParameter,
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
        },
        beforeDestroy() {
            EventBus.$off('forward-data', this.acceptForwardData);
        },
        methods: {
            acceptForwardData(data: string): void {
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
            showAlignmentViewer(): void {
                EventBus.$emit('show-modal', {
                    id: 'alignmentViewer',
                    props: {
                        sequences: this.input,
                        format: this.detectedFormat.toLowerCase(),
                    },
                });
            },
            computeOutput(selectedFormat: SelectOption): void {
                if (selectedFormat !== undefined && this.reformat !== undefined) {
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
        font-size: 0.8em;
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
