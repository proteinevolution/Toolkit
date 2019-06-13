<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model.number="submissionValue"
                      type="number"
                      :min="parameter.min"
                      :max="parameter.max"
                      :step="parameter.step"
                      :state="hasError ? false : null"
                      :aria-describedby="parameter.name + '-invalid'"
                      size="sm"
                      required>
        </b-form-input>
        <b-form-invalid-feedback :id="parameter.name + '-invalid'"
                                 v-if="hasError"
                                 v-text="errorMessage"/>
    </b-form-group>
</template>

<script lang="ts">
    import {NumberParameter} from '@/types/toolkit/tools';
    import {ConstraintError} from '@/types/toolkit/validation';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import mixins from 'vue-typed-mixins';

    export default mixins(ToolParameterMixin).extend({
        name: 'NumberParameter',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => NumberParameter,
        },
        computed: {
            defaultSubmissionValue(): any {
                // overrides property in ToolParameterMixin
                return this.parameter.default || 0;
            },
        },
        watch: {
            submissionValue: {
                immediate: true,
                handler(value: number) {
                    const error: ConstraintError | undefined = this.validate(value);
                    this.setError(error);
                },
            },
        },
        methods: {
            validate(value: number): ConstraintError | undefined {
                if (!value && value !== 0) {
                    return {
                        textKey: 'constraints.notEmpty',
                    };
                } else if (value < this.parameter.min || value > this.parameter.max) {
                    return {
                        textKey: 'constraints.range',
                        textKeyParams: {min: this.parameter.min, max: this.parameter.max},
                    };
                }
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>