<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model="text"
                      type="text"
                      size="sm"
                      required>
        </b-form-input>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {TextInputParameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'TextInputParameter',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextInputParameter,
        },
        data() {
            return {
                text: '',
            };
        },
    });
</script>

<style lang="scss" scoped>

</style>
