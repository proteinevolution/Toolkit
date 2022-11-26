<template>
    <div class="custom-job-id">
        <b-form-input
            v-model="customJobId"
            v-b-tooltip.hover="t('constraints.customerJobIdTooShort')"
            :placeholder="t('tools.parameters.customJobId.placeholder')"
            aria-describedby="custom-job-id-invalid"
            :state="valid"
            @input="inputChanged" />
        <b-form-invalid-feedback v-if="hasError" id="custom-job-id-invalid">
            <i18n-t :keypath="error.textKey">
                <span class="suggestion-link" @click="takeSuggestion">{{ suggestion }}</span>
            </i18n-t>
        </b-form-invalid-feedback>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, toRefs } from 'vue';
import { authService } from '@/services/AuthService';
import { SimpleToolParameterProps, useToolParameterValidation } from '@/composables/useToolParameter';
import { ConstraintError } from '@/types/toolkit/validation';
import { CustomJobIdValidationResult } from '@/types/toolkit/jobs';
import { debounce } from 'lodash-es';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// We need to manually declare the props here because of how defineProps is defined
interface CustomJobIdInputProps extends SimpleToolParameterProps {
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
}

const props = defineProps<CustomJobIdInputProps>();
const { validationErrors, submission } = toRefs(props);

const suggestion = ref('');
const parameterName = ref('jobID');

const { error, hasError, setError } = useToolParameterValidation({
    parameterName,
    validationErrors,
});

const customJobId = computed({
    // handle submission manually (not via ToolParameterMixin) to exclude empty strings
    get(): string {
        if (!(parameterName.value in submission.value)) {
            return '';
        }
        return submission.value[parameterName.value];
    },
    set(value: string) {
        // don't set submission if its empty
        if (value !== '') {
            submission.value[parameterName.value] = value;
        } else {
            delete submission.value[parameterName.value];
        }
    },
});

const valid = computed<boolean | null>(() => {
    if (customJobId.value.length === 0) {
        return null;
    }
    return !hasError;
});

function validateCustomJobId(value: string) {
    authService.validateJobId(value).then((result: CustomJobIdValidationResult) => {
        if (customJobId.value === value) {
            // only update the error if value hasn't changed since api call
            const error: ConstraintError | undefined = !result.exists
                ? undefined
                : {
                      textKey: 'constraints.invalidCustomJobId',
                  };
            setError(error);
            suggestion.value = result.suggested ? result.suggested : '';
        }
    });
}

const debouncedValidateCustomJobId = debounce(validateCustomJobId, 400);

function inputChanged(value: string) {
    if (value.length === 0) {
        setError(undefined);
        return;
    } else if (value.length < 3) {
        setError({
            textKey: 'constraints.customerJobIdTooShort',
        });
        return;
    }
    debouncedValidateCustomJobId(value);
}

function takeSuggestion() {
    customJobId.value = suggestion.value;
    validateCustomJobId(customJobId.value);
}
</script>

<style lang="scss" scoped>
.suggestion-link {
    text-decoration: underline;
    color: $primary;
    cursor: pointer;
}
</style>
