<template>
    <b-form-group :label="t('tools.parameters.labels.' + parameter.name)">
        <switches v-model="submissionValue" />
    </b-form-group>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import Switches from 'vue-switches';
import { BooleanParameter } from '@/types/toolkit/tools';
import useToolParameter, { ToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<ToolParameterProps<BooleanParameter> & { enabledOverride: boolean }>();

watch(
    () => props.enabledOverride,
    (enabledOverride) => (submissionValue.value = enabledOverride || submissionValue.value)
);

const defaultSubmissionValue = computed(() => props.enabledOverride || props.parameter.default);

const { submissionValue } = useToolParameter({
    props,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
    rememberParameters: ref(true),
});
</script>
