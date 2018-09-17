<template>
    <div>
        <b-form-textarea class="textarea-input"
                         v-model="input"
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
                    <multiselect @change="computeOutput"
                                 :allowEmpty="true"
                                 :options="outputFormatOptions"
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
                                 :allowEmpty="true"
                                 :options="forwardingOptions"
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
    import {ReformatViewParameter} from '../../../types/toolkit';
    import {Reformat} from '../../../modules/reformat';
    import Multiselect from 'vue-multiselect';
    import Select from './Select.vue';

    export default Vue.extend({
        name: 'ReformatView',
        data() {
            return {
                input: '',
                output: '',
                outputFormatOptions: [
                    {value: 'fasta', text: 'FASTA', $isDisabled: false},
                    {value: 'clustal', text: 'CLUSTAL', $isDisabled: false},
                ],
                forwardingOptions: [
                    {value: 'hhblits', text: 'HHBlits', $isDisabled: false},
                    {value: 'test', text: 'Test', $isDisabled: false},
                ],
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
            reformat(): Reformat {
                return new Reformat(this.input);
            },
        },
        methods: {
            handlePasteExample(): void {
                this.input = this.parameter.sampleInput;
            },
            computeOutput(value: string): void {
                if (value !== undefined &&
                    this.reformat !== undefined &&
                    this.reformat.reformat !== undefined) {
                    this.output = this.reformat.reformat(value);
                }
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
