<template>
    <div class="d-flex">
        <switches v-model="submissionValue" :label="t('tools.parameters.emailNotification')" />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Switches from 'vue-switches';
import useToolParameter, { ToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { Parameter, ValidationParams } from '@/types/toolkit/tools';
import { ConstraintError } from '@/types/toolkit/validation';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface EmailNotificationSwitchProps extends ToolParameterProps {
    parameter: Parameter;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
}

const props = defineProps<EmailNotificationSwitchProps>();

const parameterName = ref('emailUpdate');
const defaultSubmissionValue = ref(false);

const { submissionValue } = useToolParameter({
    props,
    overrideParameterName: parameterName,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
});
</script>
