<template>
    <b-form-group :label="parameter.label">
        <switches v-model="enabled">
        </switches>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Switches from 'vue-switches';
    import {BooleanParameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'Number',
        components: {
            Switches,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => BooleanParameter,
        },
        data() {
            return {
                enabled: this.parameter.default,
            };
        },
    });
</script>

<style lang="scss" scoped>

</style>