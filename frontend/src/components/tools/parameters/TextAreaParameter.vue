<template>
    <div>
        <ExpandHeight>
            <TextAreaSubComponent :parameter="parameter"
                                  :validation-params="validationParams"
                                  v-model="text"
                                  @validation="handleValidation">
            </TextAreaSubComponent>
        </ExpandHeight>
        <ExpandHeight>
            <TextAreaSubComponent v-if="secondTextAreaEnabled"
                                  :second="true"
                                  :parameter="parameter"
                                  :validation-params="validationParams"
                                  v-model="textSecond"
                                  @validation="handleValidationSecond">
            </TextAreaSubComponent>
        </ExpandHeight>
        <b-form-group v-if="parameter.allowsTwoTextAreas"
                      :label="$t('tools.parameters.textArea.alignTwoSeqToggle')">
            <switches v-model="secondTextAreaEnabled">
            </switches>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Switches from 'vue-switches';
    import mixins from 'vue-typed-mixins';
    import TextAreaSubComponent from './TextAreaSubComponent.vue';
    import {TextAreaParameter, ValidationParams} from '@/types/toolkit/tools';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {ValidationResult} from '@/types/toolkit/validation';

    export default mixins(ToolParameterMixin).extend({
        name: 'TextAreaParameter',
        components: {
            Switches,
            TextAreaSubComponent,
            ExpandHeight,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextAreaParameter,
            validationParams: Object as () => ValidationParams,
            validationErrors: Object,
            submission: Object,
        },
        data() {
            return {
                secondTextAreaEnabled: false,
                text: this.$route.params.input ? this.$route.params.input : '',
                textSecond: '',
            };
        },
        watch: {
            text: {
                immediate: true,
                handler(value: string) {
                    this.setSubmissionValue(value);
                },
            },
            textSecond(value: string) {
                if (value) {
                    Vue.set(this.submission, this.parameterName + '_two', value);
                } else {
                    Vue.delete(this.submission, this.parameterName + '_two');
                }
            },
            secondTextAreaEnabled(value: boolean) {
                if (!value) {
                    this.textSecond = '';
                    Vue.delete(this.submission, this.parameterName + '_two');
                    Vue.delete(this.validationErrors, this.parameterName + '_two');
                }
            },
        },
        methods: {
            handleValidation(val: ValidationResult) {
                if (val.failed) {
                    this.setError({textKey: val.textKey, textKeyParams: val.textKeyParams});
                } else if (this.text === '') {
                    this.setError({textKey: 'constraints.notEmpty'});
                } else {
                    this.setError(undefined);
                }
            },
            handleValidationSecond(val: ValidationResult) {
                if (val.failed) {
                    Vue.set(this.validationErrors, this.parameterName + '_two',
                        {textKey: val.textKey, textKeyParams: val.textKeyParams});
                } else {
                    Vue.delete(this.validationErrors, this.parameterName + '_two');
                }
            },
        },
    });
</script>

<style lang="scss">
    .vue-switcher-theme--default.vue-switcher-color--default div {
        background-color: lighten($primary, 15%);
    }

    .vue-switcher-theme--default.vue-switcher-color--default div:after {
        background-color: $primary;
    }
</style>

<style lang="scss" scoped>

</style>
