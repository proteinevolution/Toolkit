<template>
    <b-form-group :label="parameter.label">
        <Select :parameter="alignmodeParameter"
                ref="alignmode">
        </Select>
        <div :class="disabled ? 'disabled' : ''">
            <Boolean :parameter="macmodeParameter"
                     ref="macmode">
            </Boolean>
        </div>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Select from './Select.vue';
    import Boolean from './Boolean.vue';
    import {Parameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'AlignmentMode',
        components: {
            Select,
            Boolean,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => Parameter,
        },
        data() {
            return {
                alignmodeParameter: {
                    type: 'Select',
                    name: 'alignmode',
                    label: 'Alignment Mode',
                    options: [{value: 'local', text: 'local'}, {value: 'global', text: 'global'}],
                    maxSelectedOptions: 1,
                },
                macmodeParameter: {
                    type: 'Boolean',
                    name: 'macmode',
                    label: 'Realign with MAC',
                    default: false,
                },
            };
        },
        computed: {
            disabled() {
                return true;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .disabled {
        opacity: 0.5;
        pointer-events: none;
    }
</style>