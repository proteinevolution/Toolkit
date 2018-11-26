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
    import Vue from 'vue';
    import {TextInputParameter} from '@/types/toolkit';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';

    export default Vue.extend({
        name: 'TextInputParameter',
        mixins: [ToolParameterMixin],
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
            regex() {
                return this.parameter.regex ? new RegExp(this.parameter.regex, 'g') : null;
            },
        },
        watch: {
            text: {
                immediate: true,
                handler(value: string) {
                    this.setSubmissionValue(value);
                    if (this.parameter.regex && !this.regex.test(value)) {
                        this.setError({textKey: 'constraints.format'});
                    } else {
                        this.setError(null);
                    }
                },
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
