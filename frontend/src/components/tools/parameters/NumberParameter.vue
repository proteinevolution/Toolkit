<template>
    <b-form-group :label="$t('tools.parameters.labels.' + parameter.name)">
        <b-form-input v-model.number="submissionValue"
                      type="number"
                      :class="{nonDefault: isNonDefaultValue}"
                      :min="parameter.min"
                      :max="parameter.max"
                      :step="parameter.step"
                      :state="hasError ? false : null"
                      :aria-describedby="parameter.name + '-invalid'"
                      size="sm"
                      :placeholder="parameter.default + ' (default)'"
                      :title="parameter.default + ' (default)'"
                      required />
        <b-form-invalid-feedback v-if="hasError"
                                 :id="parameter.name + '-invalid'"
                                 v-text="errorMessage" />
    </b-form-group>
</template>

<script lang="ts">
import {NumberParameter} from '@/types/toolkit/tools';
import {ConstraintError} from '@/types/toolkit/validation';
import ParameterRememberMixin from '@/mixins/ParameterRememberMixin';

export default ParameterRememberMixin.extend({
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
            } else if (this.parameter.min && value < this.parameter.min ||
                this.parameter.max && value > this.parameter.max) {
                return {
                    textKey: 'constraints.range',
                    textKeyParams: {min: this.parameter.min, max: this.parameter.max},
                };
            }
        },
        submissionValueFromString(value: string): number {
            return parseFloat(value);
        },
    },
});
</script>

<style lang="scss" scoped>
.nonDefault {
  background: $non-default-highlight;
}
</style>
