<template>
    <div>
        <b-row>
            <b-col v-for="parameter in section.parameters"
                   :key="parameter.name"
                   :md="mediumSize(parameter)"
                   :sm="smallSize(parameter)"
                   :lg="largeSize(parameter)">
                <component :is="parameter.parameterType"
                           :parameter="parameter"
                           :validation-params="validationParams"
                           :validation-errors="validationErrors"
                           :submission="submission"
                           :remember-params="rememberParams"
                           :class="['size-' + mediumSize(parameter)]"
                           class="parameter-component"/>
            </b-col>
        </b-row>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import TextInputParameter from './TextInputParameter.vue';
    import TextAreaParameter from './TextAreaParameter.vue';
    import SelectParameter from './SelectParameter.vue';
    import NumberParameter from './NumberParameter.vue';
    import BooleanParameter from './BooleanParameter.vue';
    import ModellerParameter from './special/ModellerParameter.vue';
    import AlignmentViewerView from './special/AlignmentViewerView.vue';
    import ReformatView from './special/ReformatView.vue';
    import {Parameter, ParameterSection, ValidationParams} from '@/types/toolkit/tools';
    import {ParameterType} from '@/types/toolkit/enums';
    import HHpredSelectsParameter from '@/components/tools/parameters/special/HHpredSelectsParameter.vue';

    export default Vue.extend({
        name: 'Section',
        components: {
            TextInputParameter,
            TextAreaParameter,
            SelectParameter,
            NumberParameter,
            BooleanParameter,
            ModellerParameter,
            HHpredSelectsParameter,
            AlignmentViewerView,
            ReformatView,
        },
        props: {
            section: Object as () => ParameterSection,
            validationParams: Object as () => ValidationParams,
            validationErrors: Object,
            submission: Object,
            rememberParams: Object,
            fullScreen: {
                type: Boolean,
                default: false,
                required: false,
            },
        },
        methods: {
            largeSize(parameter: Parameter) {
                if (parameter.parameterType === ParameterType.HHpredSelectsParameter) {
                    return 12;
                } else if (this.section.multiColumnLayout) {
                    return this.fullScreen ? 3 : 4;
                } else if (
                    parameter.parameterType === ParameterType.TextAreaParameter ||
                    parameter.parameterType === ParameterType.ReformatView ||
                    parameter.parameterType === ParameterType.AlignmentViewerView
                ) {
                    return 12;
                } else {
                    return this.fullScreen ? 4 : 6;
                }
            },
            mediumSize(parameter: Parameter) {
                if (parameter.parameterType === ParameterType.HHpredSelectsParameter) {
                    return 12;
                } else if (this.section.multiColumnLayout) {
                    return 4;
                } else if (
                    parameter.parameterType === ParameterType.TextAreaParameter ||
                    parameter.parameterType === ParameterType.ReformatView ||
                    parameter.parameterType === ParameterType.AlignmentViewerView
                ) {
                    return 12;
                } else {
                    return 6;
                }
            },
            smallSize(parameter: Parameter): number {
                if (parameter.parameterType === ParameterType.HHpredSelectsParameter) {
                    return 12;
                }
                return this.section.multiColumnLayout ? 6 : 12;
            },
        },
    });
</script>

<style lang="scss">
    .parameter-component {
        width: 100%;
        max-width: 15rem;

        .col-form-label {
            font-size: 0.8em;
        }

        &.size-12 {
            max-width: none;
        }
    }
</style>
