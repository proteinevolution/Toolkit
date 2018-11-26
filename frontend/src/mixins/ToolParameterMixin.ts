import Vue from 'vue';
import {ConstraintError} from '@/types/toolkit/validation';
import {TranslateResult} from 'vue-i18n';

const ToolParameterMixin = Vue.extend({
    props: {
        parameter: Object,
        validationErrors: Object,
        submission: Object,
    },
    computed: {
        parameterName(): string {
            return this.parameter.name;
        },
        error(): ConstraintError {
            return this.validationErrors[this.parameterName];
        },
        hasError(): boolean {
            return this.error != null;
        },
        errorMessage(): TranslateResult {
            return this.$t(this.error.textKey, this.error.textKeyParams);
        },
    },
    methods: {
        setError(error: ConstraintError) {
            if (error) {
                Vue.set(this.validationErrors, this.parameterName, error);
            } else {
                Vue.delete(this.validationErrors, this.parameterName);
            }
        },
        setSubmissionValue(value: any) {
            Vue.set(this.submission, this.parameterName, value);
        },
    },
});

export default ToolParameterMixin;
