import {ParameterType} from '../../../types/toolkit/enums';
import {ParameterType} from '../../../types/toolkit/enums';
<template>
    <div>
        <b-row>
            <b-col v-for="parameter in section.parameters"
                   :sm="section.multiColumnLayout ? 6 : 12"
                   :md="mediumSize(parameter)"
                   :key="parameter.name">
                <component :is="parameter.parameterType"
                           :parameter="parameter"
                           :validation-params="validationParams"
                           :validation-errors="validationErrors"
                           :submission="submission"
                           class="parameter-component">
                </component>
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
    import ModellerParameter from './ModellerParameter.vue';
    import AlignmentViewerView from './AlignmentViewerView.vue';
    import ReformatView from './ReformatView.vue';
    import {Parameter, ParameterSection, ValidationParams} from '@/types/toolkit/tools';
    import {ParameterType} from '@/types/toolkit/enums';

    export default Vue.extend({
        name: 'Section',
        components: {
            TextInputParameter,
            TextAreaParameter,
            SelectParameter,
            NumberParameter,
            BooleanParameter,
            ModellerParameter,
            AlignmentViewerView,
            ReformatView,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            section: Object as () => ParameterSection,
            validationParams: Object as () => ValidationParams,
            validationErrors: Object,
            submission: Object,
        },
        methods: {
            mediumSize(parameter: Parameter) {
                if (this.section.multiColumnLayout) {
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
        },
    });
</script>

<style lang="scss" scoped>
    .parameter-component {
        width: 100%;
    }
</style>
