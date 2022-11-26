<template>
    <b-form-group :label="t('tools.parameters.labels.' + parameter.name)">
        <b-form-input
            v-model.number="submissionValue"
            type="number"
            :class="{ nonDefault: isNonDefaultValue }"
            :min="parameter.min"
            :max="parameter.max"
            :step="parameter.step"
            :state="hasError ? false : null"
            :aria-describedby="parameter.name + '-invalid'"
            size="sm"
            :placeholder="parameter.default + ' (default)'"
            :title="parameter.default + ' (default)'"
            required />
        <b-form-invalid-feedback v-if="hasError" :id="parameter.name + '-invalid'" v-text="errorMessage" />
    </b-form-group>
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue';
import { NumberParameter, ValidationParams } from '@/types/toolkit/tools';
import { ConstraintError } from '@/types/toolkit/validation';
import useToolParameter, { ToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface NumberParameterProps extends ToolParameterProps<NumberParameter> {
    parameter: NumberParameter;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
}

const props = defineProps<NumberParameterProps>();
const parameter = toRef(props, 'parameter');

const defaultSubmissionValue = computed(() => parameter.value.default ?? 0);

const { submissionValue, isNonDefaultValue, errorMessage, hasError, setError } = useToolParameter({
    props,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): number => parseFloat(value),
    rememberParameters: ref(true),
});

function validate(value: number): ConstraintError | undefined {
    if (!value && value !== 0) {
        return {
            textKey: 'constraints.notEmpty',
        };
    } else if (
        (parameter.value.min && value < parameter.value.min) ||
        (parameter.value.max && value > parameter.value.max)
    ) {
        return {
            textKey: 'constraints.range',
            textKeyParams: { min: parameter.value.min, max: parameter.value.max },
        };
    }
}

watch(
    submissionValue,
    (value: number) => {
        const error: ConstraintError | undefined = validate(value);
        setError(error);
    },
    { immediate: true }
);
</script>

<style lang="scss" scoped>
.nonDefault {
    background: $non-default-highlight;
}
</style>
