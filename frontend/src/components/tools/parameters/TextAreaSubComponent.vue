<template>
    <b-form-group class="textarea-group"
                  :class="[uploadingFile ? 'uploading-file' : '']">
        <b-form-textarea class="textarea-alignment"
                         :placeholder="parameter.inputPlaceholder"
                         v-model="text"
                         cols="70"
                         spellcheck="false">
        </b-form-textarea>
        <b-progress :value="fileUploadProgress"
                    class="file-upload-progress"
                    :max="100"/>
        <b-button-group size="sm"
                        class="mt-1 mb-3">
            <b-btn variant="link"
                   @click="handlePasteExample">
                {{ $t('tools.parameters.textArea.pasteExample') }}
            </b-btn>
            <label class="btn btn-link mb-0">
                {{ $t('tools.parameters.textArea.uploadFile') }}
                <input type="file"
                       class="d-none"
                       @change="handleFileUpload"/>
            </label>
        </b-button-group>
        <VelocityFade v-if="text">
            <b-alert show
                     key="autoTransformMessage"
                     v-if="autoTransformedParams"
                     variant="success"
                     class="validation-alert mb-0 mr-2">
                {{ $t('tools.validation.autoTransformedToFasta', autoTransformedParams) }}
            </b-alert>
            <b-alert show
                     key="validationMessage"
                     v-if="validation.cssClass && !autoTransformedParams"
                     :variant="validation.cssClass"
                     class="validation-alert mb-0">
                {{ $t('tools.validation.' + validation.textKey, validation.textKeyParams) }}
            </b-alert>
        </VelocityFade>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {TextAreaParameter, ValidationParams} from '../../../types/toolkit';
    import {transformToFormat, validation} from '@/util/validation';
    import {ValidationResult} from '@/types/toolkit/validation';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import SampleSeqs from '@/conf/SampleSeqs';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        mixins: [ToolParameterMixin],
        components: {
            VelocityFade,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: {
                type: Object as () => TextAreaParameter,
            },
            validationParams: {
                type: Object as () => ValidationParams,
            },
            input: {
                type: String,
                required: false,
                default: undefined,
            },
            second: {
                type: Boolean,
                required: false,
                default: false,
            },
        },
        data() {
            return {
                text: this.input ? this.input : '',
                fileUploadProgress: 0,
                uploadingFile: false,
                autoTransformedParams: null,
                autoTransformMessageTimeout: 2500,
            };
        },
        watch: {
            text: {
                immediate: true,
                handler(value: string) {
                    this.setSubmissionValue(value);
                    // check for empty text here, somehow the computed property is not recalculated upon clearing the input
                    if (!value) {
                        this.setError({textKey: 'constraints.notEmpty'});
                    }
                },
            },
        },
        computed: {
            parameterName(): string {
                return this.parameter.name + (this.second ? '_two' : '');
            },
            validation(): ValidationResult {
                const val: ValidationResult = validation(this.text, this.parameter.inputType, this.validationParams);
                if (val.textKey === 'shouldAutoTransform') {
                    this.text = transformToFormat(this.text, val.textKeyParams.transformFormat);
                    this.displayAutoTransformMessage(val.textKeyParams);

                    // trigger validation again
                    return validation(this.text, this.parameter.inputType, this.validationParams);
                }
                // propagate error
                this.setError(val.failed ? {textKey: val.textKey, textKeyParams: val.textKeyParams} : null);
                return val;
            },
        },
        methods: {
            handleFileUpload($event: Event): void {
                const fileUpload: HTMLInputElement = $event.target as HTMLInputElement;
                if (fileUpload.files && fileUpload.files.length > 0) {
                    this.fileUploadProgress = 0;
                    this.uploadingFile = true;
                    const file = fileUpload.files[0];
                    fileUpload.value = ''; // reset file upload
                    // TODO validate MIME type
                    const reader = new FileReader();
                    reader.onload = () => {
                        if (reader.result) {
                            this.text = reader.result.toString();
                        }
                    };
                    reader.onerror = this.errorHandler;
                    reader.onprogress = (evt: ProgressEvent) => {
                        if (evt.lengthComputable) {
                            this.fileUploadProgress = Math.round((evt.loaded / evt.total) * 100);
                        }
                    };
                    reader.onloadend = () => {
                        setTimeout(() => {
                            this.$alert(this.$t('tools.parameters.textArea.uploadedFile'));
                            this.uploadingFile = false;
                        }, 500);
                    };
                    reader.readAsText(file);
                }
            },
            displayAutoTransformMessage(params: any): void {
                this.autoTransformedParams = params;
                setTimeout(() => {
                    this.autoTransformedParams = null;
                }, this.autoTransformMessageTimeout);
            },
            errorHandler(evt: Event): void {
                const error = (evt.target as FileReader).error;
                this.uploadingFile = false;
                if (error) {
                    switch (error.code) {
                        case error.NOT_FOUND_ERR:
                            this.$alert(this.$t('errors.fileNotFound'), '', 'danger');
                            break;
                        case error.ABORT_ERR:
                            break; // noop
                        default:
                            this.$alert(this.$t('errors.fileUnreadable'), '', 'danger');
                    }
                }
            },
            handlePasteExample(): void {
                const sampleSeqKeys: string[] = this.parameter.sampleInputKey.split(',');
                this.text = SampleSeqs[sampleSeqKeys[this.second ? 1 : 0]];
            },
        },
    });
</script>

<style lang="scss" scoped>
    .textarea-group {
        width: 100%;

        .btn-link:hover, .btn-link:active, .btn-link:focus {
            text-decoration: none;
        }

        .file-upload-progress {
            height: 0;
            transition: height 1s;
        }

        &.uploading-file {
            .textarea-alignment {
                border-bottom-left-radius: 0;
                border-bottom-right-radius: 0;
            }

            .file-upload-progress {
                height: 1rem;
                transition: height 0s;
                border-top-left-radius: 0;
                border-top-right-radius: 0;
            }
        }
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
