<template>
    <b-form-group :label="t('tools.parameters.labels.' + parameter.name)">
        <multiselect
            v-model="selected"
            :multiple="isMulti"
            :max="isMulti ? parameter.maxSelectedOptions : null"
            :allow-empty="isMulti"
            :options="parameter.options"
            :options-limit="optionsLimit"
            track-by="value"
            label="text"
            :placeholder="
                t(isMulti ? 'tools.parameters.select.multiplePlaceholder' : 'tools.parameters.select.singlePlaceholder')
            "
            :searchable="true"
            :show-no-results="false"
            :disabled="disabled"
            select-label=""
            deselect-label=""
            selected-label=""
            :class="{ nonDefault: !disabled && isNonDefaultValue }">
            <template #maxElements>
                {{ t(maxElementTextKey) }}
            </template>
            <template #option="{ option }">
                {{ option.text + (parameter.default === option.value ? ' (default)' : '') }}
            </template>
        </multiselect>
    </b-form-group>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, toRefs, watch } from 'vue';
import Multiselect from '@suadelabs/vue3-multiselect';
import { SelectOption, SelectParameter, ValidationParams } from '@/types/toolkit/tools';
import Logger from 'js-logger';
import { useEventBus } from '@vueuse/core';
import useToolParameter, { ToolParameterProps } from '@/composables/useToolParameter';
import { useI18n } from 'vue-i18n';
import { isNullable } from '@/util/nullability-helpers';
import { ConstraintError } from '@/types/toolkit/validation';

const logger = Logger.get('SelectParameter');

const { t } = useI18n();

interface SelectParameterProps extends ToolParameterProps<SelectParameter> {
    parameter: SelectParameter;
    validationParams: ValidationParams;
    validationErrors: Record<string, ConstraintError>;
    submission: Record<string, any>;
    rememberParams: Record<string, any>;
    // Custom props
    maxElementTextKey?: string;
    disabled?: boolean;
    forceSelectNone?: boolean;
}

const props = withDefaults(defineProps<SelectParameterProps>(), {
    maxElementTextKey: 'tools.parameters.select.maxElements',
    disabled: false,
    forceSelectNone: false,
});
const { disabled, parameter, forceSelectNone } = toRefs(props);

const isMulti = computed(() => parameter.value.forceMulti || parameter.value.maxSelectedOptions > 1);
const optionsLimit = computed(() => {
    // WATCH OUT: This is a workaround to simulate setting the maximum selected options to zero.
    //       Currently, vue-multiselect interprets max == 0 as unlimited options (See:
    //       https://github.com/shentao/vue-multiselect/blob/12726abf0618acdd617a4391244f25c8a267a95d
    //       /src/multiselectMixin.js#L238)
    return parameter.value.maxSelectedOptions === 0 ? 0 : parameter.value.options.length;
});

const defaultSubmissionValue = computed(() => parameter.value.default || '');

const { submissionValue, isNonDefaultValue } = useToolParameter({
    props,
    defaultSubmissionValue,
    rememberParameters: computed(() => !disabled.value),
});

const selected = computed({
    get(): SelectOption[] {
        if (isMulti.value) {
            // submissionValue contains the selected option values separated by whitespaces in this case
            return parameter.value.options.filter((o: SelectOption) => submissionValue.value.includes(o.value));
        } else {
            return parameter.value.options.filter((o: SelectOption) => o.value === submissionValue.value);
        }
    },
    set(value: SelectOption[] | SelectOption) {
        submissionValue.value =
            value instanceof Array ? value.map((o: SelectOption) => o.value).join(' ') : value.value;
    },
});

watch(
    forceSelectNone,
    (value: boolean) => {
        if (value) {
            selected.value = [];
        }
    },
    { immediate: true }
);

function msaDetectedChanged(msaDetected: boolean): void {
    if (parameter.value.onDetectedMSA !== undefined && parameter.value.onDetectedMSA !== null) {
        const val = msaDetected ? parameter.value.onDetectedMSA : parameter.value.default;
        if (msaDetected) {
            const option = parameter.value.options.find((o: SelectOption) => o && o.value === val);
            if (isNullable(option)) {
                logger.warn(`did not find option for value ${val}`);
            } else {
                selected.value = option;
                logger.info(`msa detected: ${msaDetected}. Setting value for ${parameter.value.name}
                                to "${val}"`);
            }
        }
    }
}

const msaDetectedChangedBus = useEventBus<boolean>('msa-detected-changed');
onMounted(() => {
    if (parameter.value.onDetectedMSA !== undefined && parameter.value.onDetectedMSA !== null) {
        msaDetectedChangedBus.on(msaDetectedChanged);
    }
});
onBeforeUnmount(() => msaDetectedChangedBus.off(msaDetectedChanged));
</script>
