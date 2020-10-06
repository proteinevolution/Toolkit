<template>
    <b-form-group class="textarea-group"
                  :class="{'uploading-file': uploadingFile}">
        <b-form-textarea class="textarea-alignment break-all"
                         :placeholder="$t('tools.inputPlaceholder.' + parameter.placeholderKey)"
                         :value="value"
                         cols="70"
                         spellcheck="false"
                         @input="handleInput" />
        <input :id="'file-upload-' + parameter.name + '-' + second"
               type="file"
               :class="{'d-none': !fileDragged}"
               class="file-upload-dropzone"
               @change="handleFileUpload">
        <b-progress :value="fileUploadProgress"
                    class="file-upload-progress"
                    :max="100" />
        <b-button-group size="sm"
                        class="mt-1 mb-3">
            <b-btn data-v-step="2"
                   variant="link"
                   @click="handlePasteExample">
                <loading v-if="$store.state.loading.alignmentTextarea"
                         :size="20" />
                <span v-else
                      v-text="$t('tools.parameters.textArea.pasteExample')"></span>
            </b-btn>
            <label class="btn btn-link mb-0 cursor-pointer"
                   :for="'file-upload-' + parameter.name + '-' + second"
                   v-text="$t('tools.parameters.textArea.uploadFile')"></label>
        </b-button-group>
        <VelocityFade v-if="value">
            <b-alert v-if="autoTransformedParams"
                     key="autoTransformMessage"
                     :show="true"
                     variant="success"
                     class="validation-alert mb-0 mr-2">
                {{ $t('tools.validation.autoTransformedToFasta', autoTransformedParams) }}
            </b-alert>
            <b-alert v-if="validation.cssClass && !autoTransformedParams"
                     key="validationMessage"
                     :show="true"
                     :variant="validation.cssClass"
                     class="validation-alert mb-0">
                {{ $t('tools.validation.' + validation.textKey, validation.textKeyParams) }}
            </b-alert>
        </VelocityFade>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {TextAreaParameter, ValidationParams} from '@/types/toolkit/tools';
    import {transformToFormat, validation} from '@/util/validation';
    import {ValidationResult} from '@/types/toolkit/validation';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import EventBus from '@/util/EventBus';
    import Logger from 'js-logger';
    import {sampleSeqService} from '@/services/SampleSeqService';
    import Loading from '@/components/utils/Loading.vue';

    const logger = Logger.get('TextAreaSubComponent');

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        components: {
            VelocityFade,
            Loading,
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
            value: {
                type: String,
                required: true,
            },
            second: {
                type: Boolean,
                required: false,
                default: false,
            },
        },
        data() {
            return {
                fileUploadProgress: 0,
                fileDragged: false,
                dragInterval: undefined as any,
                uploadingFile: false,
                autoTransformedParams: null,
                autoTransformMessageTimeout: 2500,
                validation: {} as ValidationResult,
            };
        },
        watch: {
            value: {
                immediate: true,
                handler(value: string) {
                    // validate in watcher since somehow computed properties don't update on empty strings
                    const val: ValidationResult = validation(value, this.parameter.inputType, this.validationParams);
                    if (val.textKey === 'shouldAutoTransform') {
                        this.handleInput(transformToFormat(value, val.textKeyParams.transformFormat));
                        this.displayAutoTransformMessage(val.textKeyParams);

                        // trigger validation again
                        this.validation = validation(value, this.parameter.inputType, this.validationParams);
                    }

                    // emit event if msa detected (except for second input)
                    if (!this.second && val.msaDetected !== undefined && this.validation.msaDetected !== val.msaDetected) {
                        EventBus.$emit('msa-detected-changed', val.msaDetected);
                    }

                    this.validation = val;
                    this.$emit('validation', val);
                },
            },
        },
        created() {
            (this as any).boundDragOver = this.handleDragOver.bind(this);
            document.addEventListener('dragover', (this as any).boundDragOver);
        },
        beforeDestroy() {
            document.removeEventListener('dragover', (this as any).boundDragOver);
        },
        methods: {
            handleDragOver(e: Event): void {
                e.preventDefault();
                if (this.dragInterval) {
                    clearInterval(this.dragInterval);
                }

                this.dragInterval = setInterval(() => {
                    this.fileDragged = false;
                    clearInterval(this.dragInterval);
                }, 100);
                if (!this.fileDragged) {
                    this.fileDragged = true;
                }
            },
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
                            this.handleInput(reader.result.toString());
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
                            this.$alert(this.$t('errors.fileNotFound'), 'danger');
                            break;
                        case error.ABORT_ERR:
                            break; // noop
                        default:
                            this.$alert(this.$t('errors.fileUnreadable'), 'danger');
                    }
                }
            },
            handlePasteExample(): void {
                EventBus.$emit('paste-example');
                this.$store.commit('startLoading', 'alignmentTextarea');
                const sampleSeqKey: string = this.parameter.sampleInputKey.split(',')[this.second ? 1 : 0];
                sampleSeqService.fetchSampleSequence(sampleSeqKey)
                    .then((res: string) => {
                        this.handleInput(res);
                    })
                    .catch((err: any) => {
                        logger.error('error when fetching sample sequence', err);
                        this.handleInput('Error!');
                    })
                    .finally(() => {
                        this.$store.commit('stopLoading', 'alignmentTextarea');
                    });
            },
            handleInput(value: string): void {
                this.$emit('input', value);
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
        font-size: 0.9em;

        &.shrink {
            height: 14em;
        }
    }

    .validation-alert {
        margin-top: 0.5rem;
        float: right;
        padding: 0.4rem 0.5rem;
    }

    .file-upload-dropzone {
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        width: 100%;
        background-color: $tk-lighter-gray;
        border: 1px dashed $tk-light-gray;
        border-radius: $global-radius;
        z-index: 1;
        padding: 2rem;
    }
</style>
