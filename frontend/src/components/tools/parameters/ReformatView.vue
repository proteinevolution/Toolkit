import {AlignmentSeqFormat} from '../../../types/toolkit/enums';
<template>
    <div>
        <b-form-textarea class="textarea-input"
                         v-model="input"
                         @input="clearOutput"
                         cols="70"
                         spellcheck="false">
        </b-form-textarea>
        <b-button-group size="sm"
                        class="mt-1 mb-3">
            <b-btn variant="link"
                   @click="handlePasteExample">
                {{ $t('tools.parameters.textArea.pasteExample') }}
            </b-btn>
        </b-button-group>
        <b-alert show
                 v-if="detectedFormat"
                 variant="success"
                 class="validation-alert mb-0"
                 v-html="$t('tools.reformat.detectedFormat', {format: detectedFormat})">
        </b-alert>
        <b-form-group>
            <b-row align-h="center">
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
                <b-col cols="4">
                    <multiselect v-model="selectedForwardingTool"
                                 @select="forward"
                                 :allowEmpty="true"
                                 :options="forwardingOptions"
                                 :disabled="!output"
                                 track-by="value"
                                 label="text"
                                 :placeholder="$t('tools.reformat.forwardTo')"
                                 :searchable="false"
                                 selectLabel=""
                                 deselectLabel=""
                                 selectedLabel="">
                    </multiselect>
                </b-col>
            </b-row>
        </b-form-group>
        <b-form-group>
            <b-form-textarea class="textarea-output"
                             v-model="output"
                             cols="70"
                             spellcheck="false"
                             readonly>
            </b-form-textarea>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {ReformatViewParameter, SelectOption, SequenceValidationParams, Tool} from '../../../types/toolkit';
    import {Reformat} from '../../../modules/reformat';
    import Multiselect from 'vue-multiselect';
    import Select from './Select.vue';
    import {AlignmentSeqFormat} from '../../../types/toolkit/enums';

    export default Vue.extend({
        name: 'ReformatView',
        data() {
            return {
                input: '',
                output: '',
                outputFormatOptions: [
                    {value: AlignmentSeqFormat.FASTA, text: 'FASTA', $isDisabled: false},
                    {value: AlignmentSeqFormat.CLUSTAL, text: 'CLUSTAL', $isDisabled: false},
                ],
                forwardingOptions: [] as SelectOption[],
                selectedOutputFormat: undefined,
                selectedForwardingTool: undefined,
            };
        },
        components: {
            Multiselect,
            Select,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => ReformatViewParameter,
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
        methods: {
            handlePasteExample(): void {
                this.input = this.parameter.sampleInput;
            },
            computeOutput(selectedFormat: SelectOption): void {
                if (selectedFormat !== undefined && this.reformat !== undefined) {
                    this.output = this.reformat.reformat(selectedFormat.value);
                    this.forwardingOptions = this.tools
                        .filter((tool: Tool) => {
                            const allowedFormats = (tool.validationParams as SequenceValidationParams).allowedSeqFormats;
                            return allowedFormats !== undefined &&
                                allowedFormats.includes((selectedFormat.value as AlignmentSeqFormat));
                        })
                        .map((tool: Tool) => ({
                            value: tool.name,
                            text: tool.longname,
                        }));
                }
            },
            clearOutput(): void {
                this.selectedOutputFormat = undefined;
                this.selectedForwardingTool = undefined;
                this.output = '';
            },
            forward(selectedTool: SelectOption): void {
                this.$router.push({name: 'tools', params: {toolName: selectedTool.value, input: this.output}});
            },
        },
    });
</script>

<style lang="scss" scoped>
    .textarea-input, .textarea-output {
        font-family: $font-family-monospace;
        font-size: 0.8em;
        height: 15em;
    }

    .validation-alert {
        margin-top: 0.5rem;
        float: right;
        padding: 0.4rem 0.5rem;
    }
</style>
