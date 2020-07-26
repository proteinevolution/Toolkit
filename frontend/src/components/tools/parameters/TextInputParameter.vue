<template>
    <b-form-group :label="$t('tools.parameters.labels.' + parameter.name)">
        <b-form-input v-model="submissionValue"
                      :placeholder="parameter.inputPlaceholder"
                      :state="state"
                      :class="{nonDefault: !disableRemember && isNonDefaultValue}"
                      type="text"
                      size="sm"
                      required/>
    </b-form-group>
</template>

<script lang="ts">
    import {TextInputParameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import ParameterRememberMixin from '@/mixins/ParameterRememberMixin';
    import mixins from 'vue-typed-mixins';
    import EventBus from '@/util/EventBus';

    export default mixins(ToolParameterMixin, ParameterRememberMixin).extend({
        name: 'TextInputParameter',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextInputParameter,
        },
        mounted() {
            EventBus.$on('paste-example', this.handlePasteExample);
        },
        computed: {
            defaultSubmissionValue(): any {
                // overrides the property in ToolParameterMixin
                return '';
            },
            disableRemember(): boolean {
                // overrides property in ParameterRememberMixin
                return this.parameter.disableRemember || false;
            },
            state() {
                if (this.submissionValue.length === 0) {
                    return null;
                } else if (this.hasError) {
                    return false;
                } else if (this.parameter.regex) {
                    return true;
                }
                return null;
            },
            regex(): RegExp | null {
                return this.parameter.regex ? new RegExp(this.parameter.regex) : null;
            },
        },
        watch: {
            submissionValue: {
                immediate: true,
                handler(value: string) {
                    if (this.regex && !this.regex.test(value)) {
                        this.setError({textKey: 'constraints.format'});
                    } else {
                        this.setError(undefined);
                    }
                },
            },
        },
        methods: {
            handlePasteExample() {
                if (this.parameter.sampleInput) {
                    this.submissionValue = this.parameter.sampleInput;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .nonDefault {
        background: $non-default-highlight;
    }
</style>
