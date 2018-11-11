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
                    {{ $t('tools.parameters.textArea.pasteExample') }}
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
    import {FrontendToolParameter} from '@/types/toolkit/index';
    import AlignmentViewerModal from '@/components/modals/AlignmentViewerModal.vue';
    import {Reformat} from '@/modules/reformat';

    export default Vue.extend({
        name: 'AlignmentViewerView',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
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
                return this.reformat.getFormat();
            },
        },
        methods: {
            handlePasteExample() {
                this.input = this.parameter.sampleInput;
            },
            showAlignment() {
                this.$modal.show(AlignmentViewerModal,
                    {
                        sequences: this.input,
                        format: this.detectedFormat.toLowerCase(),
                    },
                    {
                        draggable: false,
                        width: '60%',
                        height: 'auto',
                        scrollable: true,
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
