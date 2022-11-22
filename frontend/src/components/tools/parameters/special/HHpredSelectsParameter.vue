<template>
    <b-row class="mt-2">
        <b-col cols="12" sm="6">
            <select-parameter-component
                :parameter="hhsuiteDBParameter"
                :validation-params="validationParams"
                :validation-errors="validationErrors"
                :submission="submission"
                :remember-params="rememberParams"
                data-v-step="structural-domain-database"
                max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                :disabled="disabled"
                :force-select-none="disabled"
                class="parameter-component size-12" />
        </b-col>

        <b-col cols="12" sm="6">
            <select-parameter-component
                :parameter="proteomesParameter"
                :validation-params="validationParams"
                :validation-errors="validationErrors"
                :submission="submission"
                :remember-params="rememberParams"
                data-v-step="proteomes"
                max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                :disabled="disabled"
                :force-select-none="disabled"
                class="parameter-component size-12" />
        </b-col>
    </b-row>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, toRefs, watch } from 'vue';
import { HHpredSelectsParameter, SelectParameter } from '@/types/toolkit/tools';
import SelectParameterComponent from '@/components/tools/parameters/SelectParameter.vue';
import { ParameterType } from '@/types/toolkit/enums';
import { ConstraintError } from '@/types/toolkit/validation';
import { useEventBus } from '@vueuse/core';
import { defineToolParameterProps } from '@/composables/useToolParameter';
import { isNullable } from '@/util/nullability-helpers';

const props = defineToolParameterProps<HHpredSelectsParameter>();
const { validationParams, validationErrors, submission, parameter, rememberParams } = toRefs(props);

const disabled = ref(false);

const selectedOptionsHHSuite = computed<number>(() => {
    if (submission.value[parameter.value.name]) {
        return submission.value[parameter.value.name].split(' ').length;
    }
    return 0;
});

const selectedOptionsProteomes = computed<number>(() => {
    if (submission.value[parameter.value.nameProteomes]) {
        return submission.value[parameter.value.nameProteomes].split(' ').length;
    }
    return 0;
});

const maxSelectedOptionsHHSuite = computed<number>(
    () => parameter.value.maxSelectedOptions - selectedOptionsProteomes.value
);

const maxSelectedOptionsProteomes = computed<number>(
    () => parameter.value.maxSelectedOptions - selectedOptionsHHSuite.value
);

const hhsuiteDBParameter = computed<SelectParameter | null>(() => {
    if (isNullable(parameter.value)) {
        return null;
    }
    return {
        name: parameter.value.name,
        options: parameter.value.options,
        default: parameter.value.default,
        parameterType: ParameterType.SelectParameter,
        maxSelectedOptions: maxSelectedOptionsHHSuite.value,
        forceMulti: true,
    };
});

const proteomesParameter = computed<SelectParameter | null>(() => {
    if (isNullable(parameter.value)) {
        return null;
    }
    return {
        name: parameter.value.nameProteomes,
        options: parameter.value.optionsProteomes,
        default: parameter.value.defaultProteomes,
        parameterType: ParameterType.SelectParameter,
        maxSelectedOptions: maxSelectedOptionsProteomes.value,
        forceMulti: true,
    };
});

const totalSelectedOptions = computed<number>(() => selectedOptionsHHSuite.value + selectedOptionsProteomes.value);

const validationError = computed<ConstraintError | undefined>(() => {
    if (totalSelectedOptions.value === 0 && !disabled.value) {
        return {
            textKey: 'constraints.notEmpty',
        };
    }
    return undefined;
});

watch(
    validationError,
    (value: ConstraintError | undefined) => {
        if (value) {
            validationErrors.value[parameter.value.name] = value;
        } else {
            delete validationErrors.value[parameter.value.name];
        }
    },
    { deep: true, immediate: true }
);

const onSecondTextAreaEnabled = (enabled: boolean): void => {
    disabled.value = enabled;
};
const secondTextAreaEnabledBus = useEventBus<boolean>('second-text-area-enabled');
const unsubscribeSecondTextAreaEnabled = secondTextAreaEnabledBus.on(onSecondTextAreaEnabled);

onBeforeUnmount(() => {
    unsubscribeSecondTextAreaEnabled();
});
</script>
