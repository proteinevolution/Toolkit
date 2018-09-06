<template>
    <b-form-group class="textarea-group">
        <b-form-textarea class="textarea-alignment"
                         :placeholder="parameter.inputPlaceholder"
                         v-model="text"
                         :rows="shrink ? 8 : 14"
                         cols="70"
                         spellcheck="false">
        </b-form-textarea>
        <b-button-group size="sm"
                        class="mt-1 mb-3">
            <b-btn variant="link">
                Paste Example
            </b-btn>
            <b-btn variant="link">
                Upload File
            </b-btn>
        </b-button-group>
        <b-alert show
                 v-if="validation.cssClass"
                 :variant="validation.cssClass"
                 class="validation-alert mb-0">
            {{ validation.text }}
        </b-alert>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {AlignmentValidationResult} from '../../../types/toolkit/validation';
    import {Reformat} from '@/modules/reformat';
    import {TextAreaParameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        props: {
            id: String,
            shrink: Boolean,
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextAreaParameter,
        },
        data() {
            return {
                text: '',
                validation: {
                    failed: false,
                    text: '',
                    cssClass: '',
                },
            };
        },
        watch: {
            text(newVal: string) {
                this.validation = this.validate(newVal);
            },
        },
        methods: {
            validate(val: string): AlignmentValidationResult {
                // TODO use more validation types depending on tool (or find dynamic solution)
                return this.basicValidation(val);
            },
            basicValidation(val: string): AlignmentValidationResult {
                let text: string = '';
                let cssClass: string = '';
                let failed: boolean = false;

                const elem: Reformat = new Reformat(val);
                (window as any).test = elem;

                if (val.length > 0) {
                    const detectedFormat: string = elem.getFormat();
                    const isFasta: boolean = elem.validate('Fasta');

                    if (detectedFormat === '') {
                        failed = true;
                        cssClass = 'danger';
                        text = 'Invalid characters. Could not detect format.';
                    } else if (!isFasta) {
                        failed = false;
                        cssClass = 'success';
                        text = `${detectedFormat} format found: Auto-transformed to FASTA`;
                        console.log(`Autotransform from ${detectedFormat}`);
                        // TODO: break up strict two way binding to prevent double check after new setting of text
                        this.text = elem.reformat('Fasta');
                    } else {
                        cssClass = 'success';
                        text = 'Protein FASTA';
                    }
                }

                return {
                    failed,
                    text,
                    cssClass,
                };
            },
        },
    });
</script>

<style lang="scss" scoped>
    .textarea-group {
        width: 100%;
    }

    .textarea-alignment {
        font-family: $font-family-monospace;
        width: 100%;
    }

    .validation-alert {
        margin-top: 0.5rem;
        float: right;
        padding: 0.4rem 0.5rem;
    }
</style>
