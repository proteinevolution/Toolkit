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
import { computed, onBeforeUnmount, onMounted, ref, toRefs, watch } from 'vue';
import Switches from 'vue-switches';
import TextAreaSubComponent from './TextAreaSubComponent.vue';
import { TextAreaParameter } from '@/types/toolkit/tools';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import useToolParameter, { ToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { ValidationResult } from '@/types/toolkit/validation';
import { useEventBus } from '@vueuse/core';
import { useRoute } from 'vue-router';

const { t } = useI18n();

type TextAreaParameterProps = ToolParameterProps<TextAreaParameter>;
const props = defineProps<TextAreaParameterProps>();
const { parameter, submission, validationErrors } = toRefs(props);

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
        if (!(parameterNameTwo.value in submission.value)) {
            return '';
        }
        return submission.value[parameterNameTwo.value];
    },
    set(value: string) {
        // don't set submission for second text area if its empty
        if (value) {
            submission.value[parameterNameTwo.value] = value;
        } else {
            delete submission.value[parameterNameTwo.value];
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
        delete validationErrors.value[parameterNameTwo.value];
    }
    secondTextAreaEnabledBus.emit(value);
});

function acceptForwardData({ data, jobID }: { data: string; jobID: string }): void {
    submissionValue.value = data;
    submission.value.parentID = jobID;
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
        validationErrors.value[parameterNameTwo.value] = {
            textKey: val.textKey,
            textKeyParams: val.textKeyParams,
        };
    } else if (submissionValueTwo.value === '') {
        validationErrors.value[parameterNameTwo.value] = { textKey: 'constraints.notEmpty' };
    } else {
        delete validationErrors.value[parameterNameTwo.value];
    }
}
</script>
