<template>
    <div class="custom-job-id">
        <b-form-input v-model="customJobId"
                      :placeholder="$t('tools.parameters.customJobId.placeholder')"
                      aria-describedby="custom-job-id-invalid"
                      :state="valid"
                      @input="inputChanged" />
        <b-form-invalid-feedback v-if="hasError"
                                 id="custom-job-id-invalid">
            <i18n :path="error.textKey"
                  tag="span">
                <span class="suggestion-link"
                      @click="takeSuggestion">{{ suggestion }}</span>
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
    import {debounce} from 'lodash-es';

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
        created() {
            (this as any).debouncedValidateCustomJobId = debounce(this.validateCustomJobId, 400);
        },
        methods: {
            inputChanged(value: string) {
                if (value.length === 0) {
                    this.setError(undefined);
                    return;
                } else if (value.length < 3) {
                    this.setError({
                        textKey: 'constraints.customerJobIdTooShort',
                    });
                    return;
                }
                (this as any).debouncedValidateCustomJobId(value);
            },
            validateCustomJobId(value: string) {
                authService.validateJobId(value)
                    .then((result: CustomJobIdValidationResult) => {
                        if (this.customJobId === value) {
                            // only update the error if value hasn't changed since api call
                            const error: ConstraintError | undefined = !result.exists ? undefined : {
                                textKey: 'constraints.invalidCustomJobId',
                            };
                            this.setError(error);
                            this.suggestion = result.suggested ? result.suggested : '';
                        }
                    });
            },
            takeSuggestion() {
                this.customJobId = this.suggestion;
                this.validateCustomJobId(this.customJobId);
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
