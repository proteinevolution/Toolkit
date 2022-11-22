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

const { t } = useI18n();

const props = defineProps<ToolParameterProps>();

const parameterName = ref('emailUpdate');
const defaultSubmissionValue = ref(false);

const { submissionValue } = useToolParameter({
    props,
    overrideParameterName: parameterName,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
});
</script>
