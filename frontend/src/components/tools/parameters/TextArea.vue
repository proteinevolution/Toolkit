<template>
    <div>
        <ExpandHeight>
            <TextAreaSubComponent :parameter="parameter">
            </TextAreaSubComponent>
        </ExpandHeight>
        <ExpandHeight>
            <TextAreaSubComponent v-if="secondTextAreaEnabled"
                                  :parameter="parameter">
            </TextAreaSubComponent>
        </ExpandHeight>
        <b-form-group v-if="parameter.allowsTwoTextAreas"
                      :label="$t('tools.parameters.alignTwoSeqToggle')">
            <switches v-model="secondTextAreaEnabled">
            </switches>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Switches from 'vue-switches';
    import TextAreaSubComponent from './TextAreaSubComponent.vue';
    import {TextAreaParameter} from '@/types/toolkit/index';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';

    export default Vue.extend({
        name: 'TextArea',
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
