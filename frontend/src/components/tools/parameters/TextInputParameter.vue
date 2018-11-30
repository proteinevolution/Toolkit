<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model="text"
                      :placeholder="parameter.inputPlaceholder"
                      :state="state"
                      type="text"
                      size="sm"
                      required>
        </b-form-input>
    </b-form-group>
</template>

<script lang="ts">
    import {TextInputParameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import mixins from 'vue-typed-mixins';

    export default mixins(ToolParameterMixin).extend({
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
        computed: {
            state() {
                if (this.text.length === 0) {
                    return null;
                } else if (this.hasError) {
                    return false;
                } else if (this.parameter.regex) {
                    return true;
                }
                return null;
            },
            regex(): RegExp | null {
                return this.parameter.regex ? new RegExp(this.parameter.regex, 'g') : null;
            },
        },
        watch: {
            text: {
                immediate: true,
                handler(value: string) {
                    this.setSubmissionValue(value);
                    if (this.regex) {
                        this.setError(this.regex.test(value) ? undefined : {textKey: 'constraints.format'});
                    } else {
                        this.setError(undefined);
                    }
                },
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
