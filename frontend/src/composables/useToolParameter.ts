import { computed, Ref, watch } from 'vue';
import { ConstraintError } from '@/types/toolkit/validation';
import { TranslateResult, useI18n } from 'vue-i18n';
import { Parameter, ValidationParams } from '@/types/toolkit/tools';
import { isNonNullable } from '@/util/nullability-helpers';

export interface ToolParameterProps<PARAM extends Parameter = Parameter> {
    parameter: PARAM;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
}

interface UseToolParameterArguments<T> {
    props: ToolParameterProps;
    defaultSubmissionValue: Ref<T>;
    // can be overridden in the component depending on the parameter type
    overrideParameterName?: Ref<string>;
    submissionValueFromString?: (value: string) => T;
    submissionValueToString?: (value: T) => string;
    // Enable parameter remembering
    rememberParameters?: Ref<boolean>;
}

export default function useToolParameter<T>({
    props,
    defaultSubmissionValue,
    overrideParameterName,
    submissionValueFromString,
    submissionValueToString,
    rememberParameters,
}: UseToolParameterArguments<T>) {
    const { t } = useI18n();

    const parameterName = overrideParameterName ?? computed(() => props.parameter.name);

    const error = computed<ConstraintError>(() => props.validationErrors[parameterName.value]);
    const hasError = computed(() => isNonNullable(error.value));
    const errorMessage = computed<TranslateResult>(() => {
        if (!hasError.value) {
            return '';
        }
        return t(error.value.textKey, error.value.textKeyParams);
    });

    function setError(error?: ConstraintError) {
        if (error) {
            props.validationErrors[parameterName.value] = error;
        } else {
            delete props.validationErrors[parameterName.value];
        }
    }

    const valueFromString = submissionValueFromString ?? ((v) => v as T);
    const valueToString = submissionValueToString ?? ((v) => String(v));

    function setSubmissionValue(value: T) {
        props.submission[parameterName.value] = valueToString(value);
    }

    const submissionValue = computed({
        get(): T {
            if (!(parameterName.value in props.submission)) {
                setSubmissionValue(defaultSubmissionValue.value);
            }
            return valueFromString(props.submission[parameterName.value]);
        },
        set(value: T) {
            setSubmissionValue(value);
        },
    });

    const isNonDefaultValue = computed(() => submissionValue.value == defaultSubmissionValue.value);

    watch(
        submissionValue,
        (value) => {
            if (rememberParameters?.value ?? false) {
                if (isNonDefaultValue.value) {
                    props.rememberParams[parameterName.value] = valueToString(value);
                } else {
                    delete props.rememberParams[parameterName.value];
                }
            }
        },
        { deep: true, immediate: true }
    );

    return { parameterName, submissionValue, isNonDefaultValue, error, hasError, errorMessage, setError };
}
