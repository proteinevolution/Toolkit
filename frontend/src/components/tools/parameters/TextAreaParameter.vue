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
import Vue, { onMounted } from 'vue';
import Switches from 'vue-switches';
import TextAreaSubComponent from './TextAreaSubComponent.vue';
import { TextAreaParameter, ValidationParams } from '@/types/toolkit/tools';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import ToolParameterMixin from '@/mixins/ToolParameterMixin';
import { ValidationResult } from '@/types/toolkit/validation';
import { useEventBus } from '@vueuse/core';

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
    setup() {
        const pasteAreaLoadedBus = useEventBus<void>('paste-area-loaded');
        onMounted(() => {
            pasteAreaLoadedBus.emit();
        });

        const forwardDataBus = useEventBus<{ data: string; jobID: string }>('forward-data');
        const secondTextAreaEnabledBus = useEventBus<boolean>('second-text-area-enabled');

        // Cannot handle completely with setup yet, since still using Mixin
        return { forwardDataBus, secondTextAreaEnabledBus };
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
        this.forwardDataBus.on(this.acceptForwardData);
    },
    beforeDestroy() {
        this.forwardDataBus.off(this.acceptForwardData);
    },
    watch: {
        secondTextAreaEnabledInternal(value: boolean) {
            if (!value) {
                this.submissionValueTwo = '';
                Vue.delete(this.validationErrors, this.parameterNameTwo);
            }
            this.secondTextAreaEnabledBus.emit(value);
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
