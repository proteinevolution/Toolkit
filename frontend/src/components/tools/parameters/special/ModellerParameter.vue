<template>
    <div>
        <b-form-group v-if="!valid" :label="t('tools.parameters.labels.' + parameter.name)">
            <b-form-input
                v-model="submissionValue"
                type="text"
                size="sm"
                :state="submissionValue.length > 0 ? valid : null"
                required />
        </b-form-group>
        <p v-else v-text="t('tools.parameters.modellerKey.stored')"></p>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue';
import { authService } from '@/services/AuthService';
import { debounce } from 'lodash-es';
import { ConstraintError } from '@/types/toolkit/validation';
import useToolParameter, { defineToolParameterProps } from '@/composables/useToolParameter';
import { useAuthStore } from '@/stores/auth';
import { useI18n } from 'vue-i18n';
import { isNonNullable } from '@/util/nullability-helpers';

const { t } = useI18n();

const props = defineToolParameterProps();
const parameter = toRef(props, 'parameter');

const defaultSubmissionValue = ref('');

const { submissionValue, setError } = useToolParameter({
    props,
    defaultSubmissionValue,
});

const valid = ref<boolean | null>(null);

async function validateModellerKey(value: string) {
    const result = await authService.validateModellerKey(value);
    const error: ConstraintError | undefined = isNonNullable(result)
        ? undefined
        : {
              textKey: 'constraints.invalidModellerKey',
          };
    setError(error);
    valid.value = result;
}

const debouncedValidateModellerKey = debounce(validateModellerKey, 500);

watch(
    submissionValue,
    (modellerKey) => {
        valid.value = null;
        debouncedValidateModellerKey(modellerKey);
    },
    { immediate: true }
);

const authStore = useAuthStore();
const user = computed(() => authStore.user);
watch(user, () => validateModellerKey(submissionValue.value));
</script>
