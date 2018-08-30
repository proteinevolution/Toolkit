<template>
    <b-form-group :label="parameter.label">
        <b-form-select v-model="selected"
                       :options="parameter.options"
                       class="mb-3"
                       size="sm">
        </b-form-select>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {SelectParameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'Select',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => SelectParameter,
        },
        data() {
            return {
                selected: this.parameter.options[0].value || null,
            };
        },
    });
</script>

<style lang="scss" scoped>

</style>