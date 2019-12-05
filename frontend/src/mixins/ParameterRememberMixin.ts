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
        disableRemember(): boolean {
            // can be overridden to disable remembering;
            return false;
        },
    },
    watch: {
        submissionValue: {
            immediate: true,
            handler(value: any) {
                if (!this.disableRemember) {
                    if (this.isNonDefaultValue) {
                        // @ts-ignore
                        Vue.set(this.rememberParams, this.parameterName, this.submissionValueToString(value));
                    } else {
                        // @ts-ignore
                        Vue.delete(this.rememberParams, this.parameterName);
                    }
                }
            },
        },
    },
});

export default ParameterRememberMixin;
