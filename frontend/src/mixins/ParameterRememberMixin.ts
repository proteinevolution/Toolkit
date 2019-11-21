import Vue from 'vue';

// This mixin only works in combination with the ToolParameterMixin (Thus, the ts-ignores)

const ParameterRememberMixin = Vue.extend({
    props: {
        rememberParams: Object,
    },
    computed: {
        isNonDefaultValue(): boolean {
            // @ts-ignore
            return this.submissionValue !== this.defaultSubmissionValue;
        },
    },
    watch: {
        submissionValue: {
            immediate: true,
            handler(value: any) {
                if (this.isNonDefaultValue) {
                    // @ts-ignore
                    this.rememberParams[this.parameterName] = value;
                } else {
                    // @ts-ignore
                    delete this.rememberParams[this.parameterName];
                }
            },
        },
    },
});

export default ParameterRememberMixin;
