<template>
    <div class="custom-job-id">
        <b-form-input v-model="customJobId"
                      :placeholder="$t('tools.parameters.customJobId.placeholder')"
                      aria-describedby="custom-job-id-invalid"
                      :state="valid">
        </b-form-input>
        <b-form-invalid-feedback id="custom-job-id-invalid"
                                 v-if="valid === false">
            <i18n :path="error.textKey" tag="span" for="suggestion">
                <span class="suggestion-link"
                      @click="takeSuggestion"
                >{{ suggestion }}</span>
            </i18n>
        </b-form-invalid-feedback>
    </div>
</template>

<script lang="ts">
    import AuthService from '@/services/AuthService';
    import {debounce} from 'lodash-es';
    import {Parameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {ConstraintError} from '@/types/toolkit/validation';
    import {CustomJobIdValidationResult} from '@/types/toolkit/jobs';

    export default ToolParameterMixin.extend({
        name: 'CustomJobIdInput',
        props: {},
        data() {
            return {
                customJobId: '',
                valid: null,
                suggestion: '',
            };
        },
        computed: {
            // necessary to override mixin values
            parameterName() {
                return 'jobID';
            },
        },
        watch: {
            customJobId: {
                immediate: true,
                handler(value: string) {
                    this.valid = null;
                    this.suggestion = '';
                    if (value.length > 0) {
                        this.validateCustomJobId(value);
                    }
                    this.setSubmissionValue(value);
                },
            },
        },
        methods: {
            validateCustomJobId: debounce(function(this: any, value: string) {
                AuthService.validateJobId(value)
                    .then((result: CustomJobIdValidationResult) => {
                        const error: ConstraintError | undefined = !result.exists ? undefined : {
                            textKey: 'constraints.invalidCustomJobId',
                        };
                        this.setError(error);
                        this.suggestion = result.suggested;
                        this.valid = !result.exists;
                    });
            }, 500),
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
