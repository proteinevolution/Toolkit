<template>
    <div>
        <ExpandHeight>
            <TextAreaSubComponent
                v-model:value="submissionValue"
                :parameter="parameter"
                :validation-params="validationParams"
                @validation="handleValidation" />
        </ExpandHeight>
        <ExpandHeight>
            <TextAreaSubComponent
                v-if="secondTextAreaEnabled"
                v-model:value="submissionValueTwo"
                :second="true"
                :parameter="parameter"
                :validation-params="validationParams"
                @validation="handleValidationSecond" />
        </ExpandHeight>
        <b-form-group v-if="parameter.allowsTwoTextAreas">
            <Switches v-model="secondTextAreaEnabled" :label="t('tools.parameters.textArea.alignTwoSeqToggle')" />
        </b-form-group>
    </div>
</template>

<script setup lang="ts">
import Vue, { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import Switches from 'vue-switches';
import TextAreaSubComponent from './TextAreaSubComponent.vue';
import { TextAreaParameter } from '@/types/toolkit/tools';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import useToolParameter, { defineToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { ValidationResult } from '@/types/toolkit/validation';
import { useEventBus } from '@vueuse/core';
import { useRoute } from 'vue-router';

const { t } = useI18n();

const props = defineToolParameterProps<TextAreaParameter>();
const parameter = computed(() => props.parameter);
const validationParams = computed(() => props.validationParams);

const route = useRoute();
const defaultSubmissionValue = computed(() => route.params.input ?? '');

const { parameterName, submissionValue, setError } = useToolParameter({
    props,
    defaultSubmissionValue,
});

const parameterNameTwo = computed(() => parameterName.value + '_two');
const submissionValueTwo = computed({
    // has to be handled manually, not covered by the ToolParameterMixin
    get(): string {
        if (!(parameterNameTwo.value in props.submission)) {
            return '';
        }
        return props.submission[parameterNameTwo.value];
    },
    set(value: string) {
        // don't set submission for second text area if its empty
        if (value) {
            Vue.set(props.submission, parameterNameTwo.value, value);
        } else {
            Vue.delete(props.submission, parameterNameTwo.value);
        }
    },
});

const secondTextAreaEnabledInternal = ref(false);
const secondTextAreaEnabled = computed({
    get: (): boolean => secondTextAreaEnabledInternal.value || submissionValueTwo.value.length > 0,
    set: (value: boolean): boolean => (secondTextAreaEnabledInternal.value = value),
});
const secondTextAreaEnabledBus = useEventBus<boolean>('second-text-area-enabled');
watch(secondTextAreaEnabledInternal, (value: boolean) => {
    if (!value) {
        submissionValueTwo.value = '';
        Vue.delete(props.validationErrors, parameterNameTwo.value);
    }
    secondTextAreaEnabledBus.emit(value);
});

function acceptForwardData({ data, jobID }: { data: string; jobID: string }): void {
    submissionValue.value = data;
    Vue.set(props.submission, 'parentID', jobID);
}

const forwardDataBus = useEventBus<{ data: string; jobID: string }>('forward-data');

const pasteAreaLoadedBus = useEventBus<void>('paste-area-loaded');
onMounted(() => {
    forwardDataBus.on(acceptForwardData);
    pasteAreaLoadedBus.emit();
});

onBeforeUnmount(() => {
    forwardDataBus.off(acceptForwardData);
});

function handleValidation(val: ValidationResult) {
    if (val.failed) {
        setError({ textKey: val.textKey, textKeyParams: val.textKeyParams });
    } else if (submissionValue.value === '') {
        setError({ textKey: 'constraints.notEmpty' });
    } else {
        setError(undefined);
    }
}

function handleValidationSecond(val: ValidationResult) {
    if (val.failed) {
        Vue.set(props.validationErrors, parameterNameTwo.value, {
            textKey: val.textKey,
            textKeyParams: val.textKeyParams,
        });
    } else if (submissionValueTwo.value === '') {
        Vue.set(props.validationErrors, parameterNameTwo.value, { textKey: 'constraints.notEmpty' });
    } else {
        Vue.delete(props.validationErrors, parameterNameTwo.value);
    }
}
</script>
