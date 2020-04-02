<template>
    <b-form-group :label="$t('tools.parameters.labels.' + parameter.name)">

        <multiselect v-model="selected"
                     :multiple="isMulti"
                     :max="isMulti ? parameter.maxSelectedOptions : null"
                     :allowEmpty="isMulti"
                     :options="parameter.options"
                     :optionsLimit="optionsLimit"
                     track-by="value"
                     label="text"
                     :placeholder="$t(isMulti ? 'tools.parameters.select.multiplePlaceholder' : 'tools.parameters.select.singlePlaceholder')"
                     :searchable="true"
                     :showNoResults="false"
                     :disabled="disabled"
                     selectLabel=""
                     deselectLabel=""
                     selectedLabel=""
                     :class="{nonDefault: !disabled && isNonDefaultValue}">
            <template #maxElements>{{ $t(maxElementTextKey) }}</template>
            <template slot="option" slot-scope="{ option }" v-if="parameter.default === option.value">
                {{ option.text }} (default)
            </template>
        </multiselect>

    </b-form-group>
</template>

<script lang="ts">
    import Multiselect from 'vue-multiselect';
    import {SelectOption, SelectParameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import ParameterRememberMixin from '@/mixins/ParameterRememberMixin';
    import EventBus from '@/util/EventBus';
    import mixins from 'vue-typed-mixins';
    import Logger from 'js-logger';

    const logger = Logger.get('SelectParameter');

    export default mixins(ToolParameterMixin, ParameterRememberMixin).extend({
        name: 'SelectParameter',
        components: {
            Multiselect,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => SelectParameter,
            maxElementTextKey: {
                type: String,
                required: false,
                default: 'tools.parameters.select.maxElementsSelected',
            },
            disabled: {
                type: Boolean,
                required: false,
                default: false,
            },
            forceSelectNone: {
                type: Boolean,
                required: false,
                default: false,
            },
        },
        mounted() {
            if (this.parameter.onDetectedMSA !== undefined && this.parameter.onDetectedMSA !== null) {
                EventBus.$on('msa-detected-changed', this.msaDetectedChanged);
            }
        },
        computed: {
            defaultSubmissionValue(): any {
                // overrides the property in ToolParameterMixin
                return this.parameter.default || '';
            },
            disableRemember(): boolean {
                // overrides property in ParameterRememberMixin
                return this.disabled;
            },
            selected: {
                get(): SelectOption[] {
                    if (this.isMulti) {
                        // submissionValue contains the selected option values separated by whitespaces in this case
                        return this.parameter.options.filter((o: SelectOption) => this.submissionValue.includes(o.value));
                    } else {
                        return this.parameter.options.filter((o: SelectOption) => o.value === this.submissionValue);
                    }
                },
                set(value: SelectOption[] | SelectOption) {
                    this.submissionValue = value instanceof Array ?
                        value.map((o: SelectOption) => o.value).join(' ') :
                        value.value;
                },
            },
            isMulti(): boolean {
                return this.parameter.forceMulti || this.parameter.maxSelectedOptions > 1;
            },
            optionsLimit(): number {
                // CARE: This is a workaround to simulate setting the maximum selected options to zero.
                //       Currently, vue-multiselect interprets max == 0 as unlimited options (See:
                //       https://github.com/shentao/vue-multiselect/blob/12726abf0618acdd617a4391244f25c8a267a95d
                //       /src/multiselectMixin.js#L238)
                return this.parameter.maxSelectedOptions === 0 ? 0 : this.parameter.options.length;
            },
        },
        methods: {
            msaDetectedChanged(msaDetected: boolean): void {
                if (this.parameter.onDetectedMSA !== undefined && this.parameter.onDetectedMSA !== null) {
                    const val: string = msaDetected ? this.parameter.onDetectedMSA : this.parameter.default;
                    if (msaDetected) {
                        const option: SelectOption = this.parameter.options.find((o: SelectOption) => o && o.value === val);
                        if (!option) {
                            logger.warn(`did not find option for value ${val}`);
                        } else {
                            this.selected = option;
                            logger.info(`msa detected: ${msaDetected}. Setting value for ${this.parameter.name} to "${val}"`);
                        }
                    }
                }
            },
        },
        watch: {
            forceSelectNone: {
                immediate: true,
                handler(value: number) {
                    if (value) {
                        this.selected = [];
                    }
                },
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
