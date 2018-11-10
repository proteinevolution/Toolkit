<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model="key"
                      type="text"
                      size="sm"
                      :state="valid"
                      required>
        </b-form-input>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import AuthService from '../../../services/AuthService.ts';
    import {debounce} from 'lodash-es';
    import {Parameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'ModellerParameter',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => Parameter,
        },
        data() {
            return {
                key: '',
                valid: null,
            };
        },
        watch: {
            key(value: string) {
                this.valid = null;
                this.debouncedValidateModellerKey(value);
            },
        },
        created() {
            this.debouncedValidateModellerKey = debounce(this.validateModellerKey, 500);
        },
        methods: {
            validateModellerKey(value: string) {
                const vm = this;
                AuthService.validateModellerKey(value)
                    .then((result: boolean) => {
                        vm.valid = result;
                    })
                    .catch(console.log);
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
