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
        error(): ConstraintError {
            return this.validationErrors[this.parameter.name];
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
                Vue.set(this.validationErrors, this.parameter.name, error);
            } else {
                Vue.delete(this.validationErrors, this.parameter.name);
            }
        },
        setSubmissionValue(value: any) {
            Vue.set(this.submission, this.parameter.name, value);
        },
    },
});

export default ToolParameterMixin;
