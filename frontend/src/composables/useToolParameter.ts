import Vue, { computed, Ref } from 'vue';
import { ConstraintError } from '@/types/toolkit/validation';
import { TranslateResult, useI18n } from 'vue-i18n';
import { Parameter, ValidationParams } from '@/types/toolkit/tools';
import { isNonNullable } from '@/util/nullability-helpers';

export interface ToolParameterProps<PARAM extends Parameter = Parameter> {
    parameter: PARAM;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: any;
}

export function defineToolParameterProps<PARAM extends Parameter = Parameter>(): ToolParameterProps<PARAM> {
    return defineProps<ToolParameterProps<PARAM>>();
}

interface UseToolParameterArguments {
    props: ToolParameterProps;
    defaultSubmissionValue?: Ref<any>;
    // can be overridden in the component depending on the parameter type
    overrideParameterName?: Ref<string>;
    submissionValueFromString?: (value: string) => any;
    submissionValueToString?: (value: any) => string;
}

export default function useToolParameter({
    props,
    defaultSubmissionValue,
    overrideParameterName,
    submissionValueFromString,
    submissionValueToString,
}: UseToolParameterArguments) {
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
            Vue.set(props.validationErrors, parameterName.value, error);
        } else {
            Vue.delete(props.validationErrors, parameterName.value);
        }
    }

    function setSubmissionValue(value: any) {
        Vue.set(props.submission, parameterName.value, (submissionValueToString ?? ((v) => String(v)))(value));
    }

    const submissionValue = computed({
        get(): any {
            if (!(parameterName.value in props.submission)) {
                setSubmissionValue(defaultSubmissionValue?.value);
            }
            return (submissionValueFromString ?? ((v) => v))(props.submission[parameterName.value]);
        },
        set(value: any) {
            setSubmissionValue(value);
        },
    });

    return { parameterName, submissionValue, error, hasError, errorMessage, setError };
}
