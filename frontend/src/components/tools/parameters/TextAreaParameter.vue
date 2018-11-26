<template>
    <div>
        <ExpandHeight>
            <TextAreaSubComponent :parameter="parameter"
                                  :validation-params="validationParams"
                                  :input="this.$route.params.input"
                                  :validation-errors="validationErrors"
                                  :submission="submission">
            </TextAreaSubComponent>
        </ExpandHeight>
        <ExpandHeight>
            <TextAreaSubComponent v-if="secondTextAreaEnabled"
                                  :second="true"
                                  :parameter="parameter"
                                  :validation-params="validationParams"
                                  :validation-errors="validationErrors"
                                  :submission="submission">
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
    import TextAreaSubComponent from './TextAreaSubComponent.vue';
    import {TextAreaParameter, ValidationParams} from '@/types/toolkit';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';

    export default Vue.extend({
        name: 'TextAreaParameter',
        mixins: [ToolParameterMixin],

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
            };
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
