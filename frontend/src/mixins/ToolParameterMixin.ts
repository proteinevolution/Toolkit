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
            if (!this.hasError) {
                return '';
            }
            return this.$t(this.error.textKey, this.error.textKeyParams);
        },
        defaultSubmissionValue(): any {
            // should be overridden in the component
            return undefined;
        },
        submissionValue: {
            get(): any {
                if (!this.submission.hasOwnProperty(this.parameterName)) {
                    this.setSubmissionValue(this.defaultSubmissionValue);
                }
                return this.submissionValueFromString(this.submission[this.parameterName]);
            },
            set(value: any) {
                this.setSubmissionValue(value);
            },
        },
    },
    methods: {
        setError(error?: ConstraintError) {
            if (error) {
                Vue.set(this.validationErrors, this.parameterName, error);
            } else {
                Vue.delete(this.validationErrors, this.parameterName);
            }
        },
        setSubmissionValue(value: any) {
            Vue.set(this.submission, this.parameterName, this.submissionValueToString(value));
        },
        submissionValueFromString(value: string): any {
            // can be overridden in the component depending on the parameter type
            return value;
        },
        submissionValueToString(value: any): string {
            return String(value);
        },
    },
});

export default ToolParameterMixin;
