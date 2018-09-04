<template>
    <b-form-group class="textarea-group">
        <b-form-textarea class="textarea-alignment"
                         :placeholder="inputPlaceholder"
                         v-model="text"
                         :rows="shrink ? 8 : 14"
                         cols="70"
                         spellcheck="false">
        </b-form-textarea>
        <b-button-group size="sm"
                        class="mt-1">
            <b-btn variant="link">
                Paste Example
            </b-btn>
            <b-btn variant="link">
                Upload File
            </b-btn>
        </b-button-group>
        <b-alert show
                 :variant="validationState[0]"
                 :style="{opacity: text.length > 0 ? 1 : 0}"
                 class="validation-alert">
            {{ $t('tools.validation.' + validationState[1]) }}
        </b-alert>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import * as Reformat from '@/modules/reformat';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        props: {
            id: String,
            inputPlaceholder: String,
            shrink: Boolean,
        },
        data() {
            return {
                text: '',
            };
        },
        computed: {
            validationState(): [string, string] {
                // this is just for demonstration
                // TODO put this somewhere else and get information from alignmentValidation model object
                // maybe change interface of reformat.js library? (include basic validation, sameLength, etc)
                if (Reformat.validate(this.text, 'FASTA')) {
                    if (!Reformat.reformat(this.text, 'sameLength')) {
                        return ['danger', 'sameLength'];
                    } else if (!Reformat.reformat(this.text, 'uniqueIDs')) {
                        return ['warning', 'uniqueIDs'];
                    } else {
                        return ['success', 'proteinFasta'];
                    }
                }
                return ['danger', 'invalidCharacters'];
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