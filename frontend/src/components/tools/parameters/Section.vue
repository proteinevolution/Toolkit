<template>
    <div>
        <b-row>
            <b-col v-for="parameter in section.parameters"
                   :sm="section.multiColumnLayout ? 6 : 12"
                   :md="section.multiColumnLayout ? 4 : parameter.parameterType === 'TextAreaParameter' ? 12 : 6"
                   :key="parameter.name">
                <component :is="parameter.parameterType"
                           :parameter="parameter"
                           :validation-params="validationParams"
                           :validation-states="validationStates"
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
    import AlignmentMode from './AlignmentMode.vue';
    import AlignmentViewerView from './AlignmentViewerView.vue';
    import ReformatView from './ReformatView.vue';
    import {ParameterSection} from '@/types/toolkit/index';
    import {ValidationParams} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'Section',
        components: {
            TextInputParameter,
            TextAreaParameter,
            SelectParameter,
            NumberParameter,
            BooleanParameter,
            ModellerParameter,
            AlignmentMode,
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
            validationStates: Object,
        },
    });
</script>

<style lang="scss" scoped>
    .parameter-component {
        width: 100%;
    }
</style>
