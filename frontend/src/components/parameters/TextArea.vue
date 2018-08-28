<template>
    <div>
        <TextAreaSubComponent :id="parameter.name"
                              :input_placeholder="parameter.input_placeholder">
        </TextAreaSubComponent>
        <TextAreaSubComponent v-if="secondTextAreaEnabled"
                              :id="parameter.name + '_two'"
                              :input_placeholder="parameter.input_placeholder">
        </TextAreaSubComponent>
        <div class="switch-container">
                <switches v-model="secondTextAreaEnabled">
                </switches>
            <span class="switch-label">Align two sequences or MSAs</span>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Switches from 'vue-switches';
    import TextAreaSubComponent from '@/components/parameters/TextAreaSubComponent.vue';
    import {TextAreaParameter} from '@/types/toolkit';

    export default Vue.extend({
        name: 'TextArea',
        components: {
            Switches,
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

<style scoped>
    .switch-container {
    }

    .switch-switch {
    }
    .switch-label {
        margin-left: 1em;
    }

</style>