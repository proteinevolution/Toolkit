<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model="modellerKey"
                      type="text"
                      size="sm"
                      :state="valid"
                      required>
        </b-form-input>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import AuthService from '../../../services/AuthService';
    import {debounce} from 'lodash-es';
    import {Parameter} from '@/types/toolkit';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {ConstraintError} from '@/types/toolkit/validation';

    export default Vue.extend({
        name: 'ModellerParameter',
        mixins: [ToolParameterMixin],
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => Parameter,
        },
        data() {
            return {
                modellerKey: '',
                valid: null,
            };
        },
        watch: {
            modellerKey: {
                immediate: true,
                handler(value: string) {
                    this.valid = null;
                    if (value.length > 0) {
                        this.validateModellerKey(value);
                    }
                    this.setSubmissionValue(value);
                },
            },
        },
        methods: {
            validateModellerKey: debounce(function(this: any, value: string) {
                AuthService.validateModellerKey(value)
                    .then((result: boolean) => {
                        console.log('result: ', result);
                        const error: ConstraintError | undefined = result ? undefined : {
                            textKey: 'constraints.invalidModellerKey',
                        };
                        this.setError(error);

                        this.valid = result;
                    });
            }, 500),
        },
    });
</script>

<style lang="scss" scoped>

</style>
