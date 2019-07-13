<template>
    <div class="custom-job-id">
        <b-form-input v-model="customJobId"
                      :placeholder="$t('tools.parameters.customJobId.placeholder')"
                      aria-describedby="custom-job-id-invalid"
                      :state="valid">
        </b-form-input>
        <b-form-invalid-feedback id="custom-job-id-invalid"
                                 v-if="hasError">
            <i18n :path="error.textKey" tag="span">
                <span class="suggestion-link"
                      @click="takeSuggestion"
                >{{ suggestion }}</span>
            </i18n>
        </b-form-invalid-feedback>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {authService} from '@/services/AuthService';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {ConstraintError} from '@/types/toolkit/validation';
    import {CustomJobIdValidationResult} from '@/types/toolkit/jobs';
    import mixins from 'vue-typed-mixins';

    export default mixins(ToolParameterMixin).extend({
        name: 'CustomJobIdInput',
        data() {
            return {
                suggestion: '' as string,
            };
        },
        computed: {
            parameterName() {
                // override mixin value
                return 'jobID';
            },
            customJobId: { // handle submission manually (not via ToolParameterMixin) to exclude empty strings
                get(): string {
                    if (!this.submission.hasOwnProperty(this.parameterName)) {
                        return '';
                    }
                    return this.submission[this.parameterName];
                },
                set(value: string) {
                    // don't set submission if its empty
                    if (value) {
                        Vue.set(this.submission, this.parameterName, value);
                    } else {
                        Vue.delete(this.submission, this.parameterName);
                    }
                },
            },
            valid(): boolean | null {
                if (this.customJobId.length === 0) {
                    return null;
                }
                return !this.hasError;
            },
        },
        watch: {
            customJobId: {
                immediate: true,
                handler(value: string) {
                    if (value.length === 0) {
                        this.setError(undefined);
                        return;
                    }
                    this.validateCustomJobId(value);
                },
            },
        },
        methods: {
            // TODO debounce this function. Currently, this is acting up when we debounce it. Investigate
            validateCustomJobId(value: string) {
                if (value.length < 3) {
                    this.setError({
                        textKey: 'constraints.customerJobIdTooShort',
                    });
                } else {
                    authService.validateJobId(value)
                        .then((result: CustomJobIdValidationResult) => {
                            if (this.customJobId === value) {
                                // only update the error if value hasn't changed since api call
                                const error: ConstraintError | undefined = !result.exists ? undefined : {
                                    textKey: 'constraints.invalidCustomJobId',
                                };
                                this.suggestion = result.suggested ? result.suggested : '';
                                this.setError(error);
                            }
                        });
                }
            },
            takeSuggestion() {
                this.customJobId = this.suggestion;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .suggestion-link {
        text-decoration: underline;
        color: $primary;
        cursor: pointer;
    }
</style>
