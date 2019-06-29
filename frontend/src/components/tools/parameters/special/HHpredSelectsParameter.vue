<template>
    <b-row class="mt-2">
        <b-col cols="12"
               sm="6">
            <select-parameter-component
                    :parameter="hhsuiteDBParameter"
                    :validation-params="validationParams"
                    :validation-errors="validationErrors"
                    :submission="submission"
                    max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                    class="parameter-component size-12">
            </select-parameter-component>
        </b-col>

        <b-col cols="12"
               sm="6">
            <select-parameter-component
                    :parameter="proteomesParameter"
                    :validation-params="validationParams"
                    :validation-errors="validationErrors"
                    :submission="submission"
                    max-element-text-key="tools.parameters.select.maxElementsSelectedHHpred"
                    class="parameter-component size-12">
            </select-parameter-component>
        </b-col>
    </b-row>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {HHpredSelectsParameter, SelectParameter, ValidationParams} from '@/types/toolkit/tools';
    import SelectParameterComponent from '@/components/tools/parameters/SelectParameter.vue';
    import {ParameterType} from '@/types/toolkit/enums';

    export default Vue.extend({
        name: 'HHpredSelectsParameter',
        components: {
            SelectParameterComponent,
        },
        props: {
            validationParams: Object as () => ValidationParams,
            validationErrors: Object,
            submission: Object,
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => HHpredSelectsParameter,
        },
        computed: {
            maxSelectedOptionsHHSuite(): number {
                if (this.submission[this.parameter.nameProteomes]) {
                    return this.parameter.maxSelectedOptions
                        - this.submission[this.parameter.nameProteomes].split(' ').length;
                }
                return this.parameter.maxSelectedOptions;
            },
            hhsuiteDBParameter(): SelectParameter | null {
                if (!this.parameter) {
                    return null;
                }
                return {
                    name: this.parameter.name,
                    label: this.parameter.label,
                    options: this.parameter.options,
                    default: this.parameter.default,
                    parameterType: ParameterType.SelectParameter,
                    maxSelectedOptions: this.maxSelectedOptionsHHSuite,
                    forceMulti: true,
                };
            },
            maxSelectedOptionsProteomes(): number {
                if (this.submission[this.parameter.name]) {
                    return this.parameter.maxSelectedOptions - this.submission[this.parameter.name].split(' ').length;
                }
                return this.parameter.maxSelectedOptions;
            },
            proteomesParameter(): SelectParameter | null {
                if (!this.parameter) {
                    return null;
                }
                return {
                    name: this.parameter.nameProteomes,
                    label: this.parameter.labelProteomes,
                    options: this.parameter.optionsProteomes,
                    default: this.parameter.defaultProteomes,
                    parameterType: ParameterType.SelectParameter,
                    maxSelectedOptions: this.maxSelectedOptionsProteomes,
                    forceMulti: true,
                };
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
