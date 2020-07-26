<template>
    <b-row class="mt-2">
        <b-col cols="12"
               sm="6">
            <select-parameter-component
                :parameter="hhsuiteDBParameter"
                :validation-params="validationParams"
                :validation-errors="validationErrors"
                :submission="submission"
                :remember-params="rememberParams"
                max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                :disabled="disabled"
                :force-select-none="disabled"
                class="parameter-component size-12"/>
        </b-col>

        <b-col cols="12"
               sm="6">
            <select-parameter-component
                :parameter="proteomesParameter"
                :validation-params="validationParams"
                :validation-errors="validationErrors"
                :submission="submission"
                :remember-params="rememberParams"
                max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                :disabled="disabled"
                :force-select-none="disabled"
                class="parameter-component size-12"/>
        </b-col>
    </b-row>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {HHpredSelectsParameter, SelectParameter, ValidationParams} from '@/types/toolkit/tools';
    import SelectParameterComponent from '@/components/tools/parameters/SelectParameter.vue';
    import {ParameterType} from '@/types/toolkit/enums';
    import {ConstraintError} from '@/types/toolkit/validation';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'HHpredSelectsParameter',
        components: {
            SelectParameterComponent,
        },
        props: {
            validationParams: Object as () => ValidationParams,
            validationErrors: Object,
            submission: Object,
            rememberParams: Object,
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => HHpredSelectsParameter,
        },
        data() {
            return {
                disabled: false,
            };
        },
        computed: {
            selectedOptionsHHSuite(): number {
                if (this.submission[this.parameter.name]) {
                    return this.submission[this.parameter.name].split(' ').length;
                }
                return 0;
            },
            maxSelectedOptionsHHSuite(): number {
                return this.parameter.maxSelectedOptions - this.selectedOptionsProteomes;
            },
            hhsuiteDBParameter(): SelectParameter | null {
                if (!this.parameter) {
                    return null;
                }
                return {
                    name: this.parameter.name,
                    options: this.parameter.options,
                    default: this.parameter.default,
                    parameterType: ParameterType.SelectParameter,
                    maxSelectedOptions: this.maxSelectedOptionsHHSuite,
                    forceMulti: true,
                };
            },
            selectedOptionsProteomes(): number {
                if (this.submission[this.parameter.nameProteomes]) {
                    return this.submission[this.parameter.nameProteomes].split(' ').length;
                }
                return 0;
            },
            maxSelectedOptionsProteomes(): number {
                return this.parameter.maxSelectedOptions - this.selectedOptionsHHSuite;
            },
            proteomesParameter(): SelectParameter | null {
                if (!this.parameter) {
                    return null;
                }
                return {
                    name: this.parameter.nameProteomes,
                    options: this.parameter.optionsProteomes,
                    default: this.parameter.defaultProteomes,
                    parameterType: ParameterType.SelectParameter,
                    maxSelectedOptions: this.maxSelectedOptionsProteomes,
                    forceMulti: true,
                };
            },
            totalSelectedOptions(): number {
                return this.selectedOptionsHHSuite + this.selectedOptionsProteomes;
            },
            validationError(): ConstraintError | undefined {
                if (this.totalSelectedOptions === 0 && !this.disabled) {
                    return {
                        textKey: 'constraints.notEmpty',
                    };
                }
                return undefined;
            },
        },
        watch: {
            validationError: {
                immediate: true,
                handler(value: ConstraintError | undefined) {
                    if (value) {
                        Vue.set(this.validationErrors, this.parameter.name, value);
                    } else {
                        Vue.delete(this.validationErrors, this.parameter.name);
                    }
                },
            },
        },
        mounted() {
            EventBus.$on('second-text-area-enabled', this.onSecondTextAreaEnabled);
        },
        methods: {
            onSecondTextAreaEnabled(enabled: boolean): void {
                this.disabled = enabled;
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
