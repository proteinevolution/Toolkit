<template>
    <div class="d-flex">
        <switches v-model="submissionValue" :label="t('tools.parameters.emailNotification')" />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Switches from 'vue-switches';
import { SimpleToolParameterProps, useSimpleToolParameter } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface EmailNotificationSwitchProps extends SimpleToolParameterProps {
    submission: Record<string, any>;
}

const props = defineProps<EmailNotificationSwitchProps>();

const parameterName = ref('emailUpdate');
const defaultSubmissionValue = ref(false);

const { submissionValue } = useSimpleToolParameter({
    props,
    parameterName,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
});
</script>
