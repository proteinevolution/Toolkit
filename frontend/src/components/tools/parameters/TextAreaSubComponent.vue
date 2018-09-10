<template>
    <b-form-group class="textarea-group">
        <b-form-textarea class="textarea-alignment"
                         :placeholder="parameter.inputPlaceholder"
                         v-model="text"
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
            {{ $t('tools.validation.' + validation.textKey, validation.textKeyParams) }}
        </b-alert>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {TextAreaParameter} from '../../../types/toolkit';
    import {transformToFasta, validation} from '@/util/validation';
    import {AlignmentValidationResult} from '../../../types/toolkit/validation';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        props: {
            id: String,
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextAreaParameter,
        },
        data() {
            return {
                text: '',
            };
        },
        computed: {
            validation(): AlignmentValidationResult {
                const val: AlignmentValidationResult = validation(this.text, this.parameter.alignmentValidation);
                if (val.textKey === 'shouldAutoTransform') {
                    this.text = transformToFasta(this.text);
                    val.textKey = 'autoTransformedToFasta';
                }
                return val;
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
        height: 20em;

        &.shrink {
            height: 14em;
        }
    }

    .validation-alert {
        margin-top: 0.5rem;
        float: right;
        padding: 0.4rem 0.5rem;
    }
</style>
