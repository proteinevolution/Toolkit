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
        name: 'BooleanParameter',
        components: {
            Switches,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => BooleanParameter,
            // this prop can be used by parent components to override the selection, but is not required
            enabledOverride: Boolean,
        },
        data() {
            return {
                enabled: this.enabledOverride || this.parameter.default,
            };
        },
        watch: {
            enabledOverride() {
                this.enabled = this.enabledOverride || this.enabled;
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>