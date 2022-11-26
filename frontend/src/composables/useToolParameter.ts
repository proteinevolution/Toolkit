import { computed, Ref, toRefs, watch } from 'vue';
import { ConstraintError } from '@/types/toolkit/validation';
import { TranslateResult, useI18n } from 'vue-i18n';
import { Parameter, ValidationParams } from '@/types/toolkit/tools';
import { isNonNullable } from '@/util/nullability-helpers';

// Because of limitations around the defineProps function, we cannot use the imported type directly
// and will need to copy it into the component files.
export interface SimpleToolParameterProps {
    submission: Record<string, any>;
}

interface UseSimpleToolParameterArguments<T> {
    submission: Ref<Record<string, any>>;
    defaultSubmissionValue: Ref<T>;
    parameterName: Ref<string>;
    submissionValueFromString?: (value: string) => T;
    submissionValueToString?: (value: T) => string;
}

export function useSimpleToolParameter<T>({
    submission,
    defaultSubmissionValue,
    parameterName,
    submissionValueFromString,
    submissionValueToString,
}: UseSimpleToolParameterArguments<T>) {
    const valueFromString = submissionValueFromString ?? ((v) => v as T);
    const valueToString = submissionValueToString ?? ((v) => String(v));

    function setSubmissionValue(value: T) {
        submission.value[parameterName.value] = valueToString(value);
    }

    const submissionValue = computed({
        get(): T {
            if (!(parameterName.value in submission.value)) {
                setSubmissionValue(defaultSubmissionValue.value);
            }
            return valueFromString(submission.value[parameterName.value]);
        },
        set(value: T) {
            setSubmissionValue(value);
        },
    });

    return { submissionValue, valueToString };
}

interface UseToolParameterValidationArguments {
    parameterName: Ref<string>;
    validationErrors: Ref<Record<string, ConstraintError>>;
}

export function useToolParameterValidation({ parameterName, validationErrors }: UseToolParameterValidationArguments) {
    const { t } = useI18n();
    const error = computed<ConstraintError>(() => validationErrors.value[parameterName.value]);
    const hasError = computed(() => isNonNullable(error.value));
    const errorMessage = computed<TranslateResult>(() => {
        if (!hasError.value) {
            return '';
        }
        return t(error.value.textKey, error.value.textKeyParams);
    });

    function setError(error?: ConstraintError) {
        if (error) {
            validationErrors.value[parameterName.value] = error;
        } else {
            delete validationErrors.value[parameterName.value];
        }
    }

    return { error, hasError, errorMessage, setError };
}

// Because of limitations around the defineProps function, we cannot use the imported type directly
// and will need to copy it into the component files.
export interface ToolParameterProps<PARAM extends Parameter = Parameter> extends SimpleToolParameterProps {
    parameter: PARAM;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    rememberParams: Record<string, any>;
}

interface UseToolParameterArguments<T>
    extends Omit<UseSimpleToolParameterArguments<T>, 'parameterName' | 'submission'> {
    props: ToolParameterProps;
    // can be overridden in the component depending on the parameter type
    overrideParameterName?: Ref<string>;
}

export function useToolParameter<T>({
    props,
    defaultSubmissionValue,
    overrideParameterName,
    submissionValueFromString,
    submissionValueToString,
}: UseToolParameterArguments<T>) {
    const { submission, validationErrors } = toRefs(props);
    const parameterName = overrideParameterName ?? computed(() => props.parameter.name);

    const { error, hasError, errorMessage, setError } = useToolParameterValidation({
        parameterName,
        validationErrors,
    });

    const { submissionValue, valueToString } = useSimpleToolParameter({
        submission,
        defaultSubmissionValue,
        parameterName,
        submissionValueFromString,
        submissionValueToString,
    });

    return { parameterName, submissionValue, error, hasError, errorMessage, setError, valueToString };
}

interface UseToolParameterWithRememberArguments<T> extends UseToolParameterArguments<T> {
    // Enables parameter remembering
    rememberParameters: Ref<boolean>;
}

export function useToolParameterWithRemember<T>({
    props,
    defaultSubmissionValue,
    overrideParameterName,
    submissionValueFromString,
    submissionValueToString,
    rememberParameters,
}: UseToolParameterWithRememberArguments<T>) {
    const { parameterName, submissionValue, valueToString, error, hasError, errorMessage, setError } = useToolParameter(
        {
            props,
            defaultSubmissionValue,
            overrideParameterName,
            submissionValueFromString,
            submissionValueToString,
        }
    );

    const isNonDefaultValue = computed(() => submissionValue.value !== defaultSubmissionValue.value);

    watch(
        submissionValue,
        (value) => {
            if (rememberParameters.value) {
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
