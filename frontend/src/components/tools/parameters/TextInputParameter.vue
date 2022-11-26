<template>
    <b-form-group :label="t('tools.parameters.labels.' + parameter.name)">
        <b-form-input
            v-model="submissionValue"
            :placeholder="parameter.inputPlaceholder"
            :state="state"
            :class="{ nonDefault: rememberParameters && isNonDefaultValue }"
            type="text"
            size="sm"
            required />
    </b-form-group>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, toRef, watch } from 'vue';
import { TextInputParameter, ValidationParams } from '@/types/toolkit/tools';
import { useEventBus } from '@vueuse/core';
import { ToolParameterProps, useToolParameterWithRemember } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { isNonNullable } from '@/util/nullability-helpers';
import { ConstraintError } from '@/types/toolkit/validation';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface TextInputParameterProps extends ToolParameterProps<TextInputParameter> {
    parameter: TextInputParameter;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
}

const props = defineProps<TextInputParameterProps>();
const parameter = toRef(props, 'parameter');
const rememberParameters = computed(() => !(parameter.value.disableRemember ?? false));

const defaultSubmissionValue = ref('');

const { submissionValue, isNonDefaultValue, hasError, setError } = useToolParameterWithRemember({
    props,
    defaultSubmissionValue,
    rememberParameters,
});

const state = computed<boolean | null>(() => {
    if (submissionValue.value.length === 0) {
        return null;
    } else if (hasError) {
        return false;
    } else if (parameter.value.regex) {
        return true;
    }
    return null;
});

const regex = computed<RegExp | null>(() =>
    isNonNullable(parameter.value.regex) ? new RegExp(parameter.value.regex) : null
);

watch(
    submissionValue,
    (value: string) => {
        if (isNonNullable(regex.value) && !regex.value.test(value)) {
            setError({ textKey: 'constraints.format' });
        } else {
            setError(undefined);
        }
    },
    { immediate: true }
);

function handlePasteExample() {
    if (parameter.value.sampleInput) {
        submissionValue.value = parameter.value.sampleInput;
    }
}

const pasteExampleBus = useEventBus<void>('paste-example');
onMounted(() => pasteExampleBus.on(handlePasteExample));
</script>

<style lang="scss" scoped>
.nonDefault {
    background: $non-default-highlight;
}
</style>
