<template>
    <div>
        <ExpandHeight>
            <TextAreaSubComponent
                v-model="submissionValue"
                :parameter="parameter"
                :validation-params="validationParams"
                @validation="handleValidation" />
        </ExpandHeight>
        <ExpandHeight>
            <TextAreaSubComponent
                v-if="secondTextAreaEnabled"
                v-model="submissionValueTwo"
                :second="true"
                :parameter="parameter"
                :validation-params="validationParams"
                @validation="handleValidationSecond" />
        </ExpandHeight>
        <b-form-group v-if="parameter.allowsTwoTextAreas">
            <switches v-model="secondTextAreaEnabled" :label="$t('tools.parameters.textArea.alignTwoSeqToggle')" />
        </b-form-group>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import Switches from 'vue-switches';
import TextAreaSubComponent from './TextAreaSubComponent.vue';
import { TextAreaParameter, ValidationParams } from '@/types/toolkit/tools';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import ToolParameterMixin from '@/mixins/ToolParameterMixin';
import { ValidationResult } from '@/types/toolkit/validation';
import EventBus from '@/util/EventBus';

export default ToolParameterMixin.extend({
    name: 'TextAreaParameter',
    components: {
        Switches,
        TextAreaSubComponent,
        ExpandHeight,
    },
    props: {
        /*
         Simply stating the interface type doesn't work, this is a workaround. See
         https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
         */
        parameter: Object as () => TextAreaParameter,
        validationParams: Object as () => ValidationParams,
        validationErrors: Object,
        submission: Object,
    },
    data() {
        return {
            secondTextAreaEnabledInternal: false,
        };
    },
    computed: {
        parameterNameTwo(): string {
            return this.parameterName + '_two';
        },
        defaultSubmissionValue(): any {
            // overrides property in ToolParameterMixin
            return this.$route.params.input ? this.$route.params.input : '';
        },
        secondTextAreaEnabled: {
            get(): boolean {
                return this.secondTextAreaEnabledInternal || this.submissionValueTwo.length > 0;
            },
            set(value: boolean): void {
                this.secondTextAreaEnabledInternal = value;
            },
        },
        submissionValueTwo: {
            // has to be handled manually, not covered by the ToolParameterMixin
            get(): string {
                if (!(this.parameterNameTwo in this.submission)) {
                    return '';
                }
                return this.submission[this.parameterNameTwo];
            },
            set(value: string) {
                // don't set submission for second text area if its empty
                if (value) {
                    Vue.set(this.submission, this.parameterNameTwo, value);
                } else {
                    Vue.delete(this.submission, this.parameterNameTwo);
                }
            },
        },
    },
    mounted() {
        EventBus.$on('forward-data', this.acceptForwardData);
        EventBus.$emit('paste-area-loaded');
    },
    beforeDestroy() {
        EventBus.$off('forward-data', this.acceptForwardData);
    },
    watch: {
        secondTextAreaEnabledInternal(value: boolean) {
            if (!value) {
                this.submissionValueTwo = '';
                Vue.delete(this.validationErrors, this.parameterNameTwo);
            }
            EventBus.$emit('second-text-area-enabled', value);
        },
    },
    methods: {
        acceptForwardData({ data, jobID }: { data: string; jobID: string }): void {
            this.submissionValue = data;
            Vue.set(this.submission, 'parentID', jobID);
        },
        handleValidation(val: ValidationResult) {
            if (val.failed) {
                this.setError({ textKey: val.textKey, textKeyParams: val.textKeyParams });
            } else if (this.submissionValue === '') {
                this.setError({ textKey: 'constraints.notEmpty' });
            } else {
                this.setError(undefined);
            }
        },
        handleValidationSecond(val: ValidationResult) {
            if (val.failed) {
                Vue.set(this.validationErrors, this.parameterNameTwo, {
                    textKey: val.textKey,
                    textKeyParams: val.textKeyParams,
                });
            } else if (this.submissionValueTwo === '') {
                Vue.set(this.validationErrors, this.parameterNameTwo, { textKey: 'constraints.notEmpty' });
            } else {
                Vue.delete(this.validationErrors, this.parameterNameTwo);
            }
        },
    },
});
</script>
