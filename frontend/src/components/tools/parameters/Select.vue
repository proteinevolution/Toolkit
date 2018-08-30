<template>
    <b-form-group :label="parameter.label">
        <b-form-select v-model="selectedSingle"
                       v-if="parameter.maxSelectedOptions === 1"
                       :options="parameter.options"
                       class="mb-3"
                       size="sm">
        </b-form-select>

        <multiselect v-if="parameter.maxSelectedOptions > 1"
                     v-model="selectedMultiple"
                     :multiple="true"
                     :max="parameter.maxSelectedOptions"
                   :options="parameter.options"
                    track-by="value"
                    label="text"
                    preselectFirst
                     :placeholder="$t('tools.parameters.multipleSelectPlaceholder')"
                     :searchable="false"
                    selectLabel=""
                    deselectLabel=""
                    selectedLabel="">
            <template slot="maxElements">Max Elements selected!</template>
        </multiselect>

    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Multiselect from 'vue-multiselect';
    import {SelectParameter} from '../../../types/toolkit';

    export default Vue.extend({
        name: 'Select',
        components: {
            Multiselect,
        },
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => SelectParameter,
        },
        data() {
            return {
                selectedSingle: this.parameter.options[0] || null,
                selectedMultiple: [],
            };
        },
    });
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

<style lang="scss">
    .multiselect__tag {
        background: $primary;
    }
    .multiselect, .multiselect__input, .multiselect__single {
        font-size: $font-size-base;
    }
    .multiselect__option--selected.multiselect__option--highlight {
        background: $primary;
        color: #fff;
    }
</style>

<style lang="scss" scoped>

</style>