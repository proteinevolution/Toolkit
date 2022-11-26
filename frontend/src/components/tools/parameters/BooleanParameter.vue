<template>
    <b-form-group :label="t('tools.parameters.labels.' + parameter.name)">
        <switches v-model="submissionValue" />
    </b-form-group>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import Switches from 'vue-switches';
import { BooleanParameter, ValidationParams } from '@/types/toolkit/tools';
import { ToolParameterProps, useToolParameterWithRemember } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { ConstraintError } from '@/types/toolkit/validation';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface BooleanParameterProps extends ToolParameterProps<BooleanParameter> {
    parameter: BooleanParameter;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
    // Custom props
    enabledOverride: boolean;
}

const props = defineProps<BooleanParameterProps>();

watch(
    () => props.enabledOverride,
    (enabledOverride) => (submissionValue.value = enabledOverride || submissionValue.value)
);

const defaultSubmissionValue = computed(() => props.enabledOverride || props.parameter.default);

const { submissionValue } = useToolParameterWithRemember({
    props,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
    rememberParameters: ref(true),
});
</script>
