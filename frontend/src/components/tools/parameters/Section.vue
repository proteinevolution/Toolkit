<template>
    <div>
        <b-row>
            <b-col v-for="parameter in section.parameters"
                   :sm="section.multiColumnLayout ? 6 : 12"
                   :md="section.multiColumnLayout ? 4 : 12"
                   :key="parameter.name">
                <component :is="parameter.type"
                           :parameter="parameter"
                           class="parameter-component">
                </component>
            </b-col>
        </b-row>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import TextArea from './TextArea.vue';
    import Select from './Select.vue';
    import Number from './Number.vue';
    import Boolean from './Boolean.vue';
    import AlignmentMode from './AlignmentMode.vue';
    import ReformatView from './ReformatView.vue';
    import {ParameterSection} from '@/types/toolkit/index';

    export default Vue.extend({
        name: 'Section',
        components: {
            TextArea,
            Select,
            Number,
            Boolean,
            AlignmentMode,
            ReformatView,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            section: Object as () => ParameterSection,
        },
    });
</script>

<style lang="scss" scoped>
    .parameter-component {
        width: 100%;
    }
</style>