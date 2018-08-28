<template>
    <div>
        <TextAreaSubComponent :id="parameter.name"
                              :input_placeholder="parameter.input_placeholder">
        </TextAreaSubComponent>
        <TextAreaSubComponent v-if="secondTextAreaEnabled"
                              :id="parameter.name + '_two'"
                              :input_placeholder="parameter.input_placeholder">
        </TextAreaSubComponent>
        <b-button :pressed.sync="secondTextAreaEnabled" variant="href">Align two sequences or MSAs</b-button>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import TextAreaSubComponent from '@/components/parameters/TextAreaSubComponent.vue';
    import {TextAreaParameter} from '@/types/toolkit';

    export default Vue.extend({
        name: 'TextArea',
        components: {
            TextAreaSubComponent,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextAreaParameter,
        },
        data() {
            return {
                secondTextAreaEnabled: false,
            };
        },
    });
</script>
