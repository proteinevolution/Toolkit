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
import { computed, ref, watch } from 'vue';
import { NumberParameter } from '@/types/toolkit/tools';
import { ConstraintError } from '@/types/toolkit/validation';
import useToolParameter, { defineToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineToolParameterProps<NumberParameter>();
const parameter = computed(() => props.parameter);

const defaultSubmissionValue = computed(() => props.parameter.default ?? 0);

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
        (props.parameter.min && value < props.parameter.min) ||
        (props.parameter.max && value > props.parameter.max)
    ) {
        return {
            textKey: 'constraints.range',
            textKeyParams: { min: props.parameter.min, max: props.parameter.max },
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
