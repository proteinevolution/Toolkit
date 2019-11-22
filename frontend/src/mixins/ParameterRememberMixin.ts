import Vue from 'vue';

// This mixin only works in combination with the ToolParameterMixin (Thus, the ts-ignores)

const ParameterRememberMixin = Vue.extend({
    props: {
        rememberParams: Object,
    },
    computed: {
        isNonDefaultValue(): boolean {
            // @ts-ignore
            // Use string comparison here since the job params are all string values upon being returned by the backend
            return String(this.submissionValue) !== String(this.defaultSubmissionValue);
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
                        Vue.set(this.rememberParams, this.parameterName, value);
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
