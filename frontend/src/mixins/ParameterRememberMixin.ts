import Vue from 'vue';
import ToolParameterMixin from '@/mixins/ToolParameterMixin';

const ParameterRememberMixin = ToolParameterMixin.extend({
    props: {
        rememberParams: Object,
    },
    computed: {
        isNonDefaultValue(): boolean {
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
                        Vue.set(this.rememberParams, this.parameterName, this.submissionValueToString(value));
                    } else {
                        Vue.delete(this.rememberParams, this.parameterName);
                    }
                }
            },
        },
    },
});

export default ParameterRememberMixin;
