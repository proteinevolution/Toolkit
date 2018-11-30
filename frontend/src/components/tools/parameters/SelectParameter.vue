<template>
    <b-form-group :label="parameter.label">

        <multiselect v-model="selected"
                     :multiple="isMulti"
                     :max="isMulti ? parameter.maxSelectedOptions : null"
                     :allowEmpty="false"
                     :options="parameter.options"
                     track-by="value"
                     label="text"
                     preselectFirst
                     :placeholder="$t(isMulti ? 'tools.parameters.select.multiplePlaceholder' : 'tools.parameters.select.singlePlaceholder')"
                     :searchable="false"
                     selectLabel=""
                     deselectLabel=""
                     selectedLabel=""
                     @input="$emit('selectionChanged', selected)">
            <template slot="maxElements">{{ $t('tools.parameters.select.maxElementsSelected') }}</template>
        </multiselect>

    </b-form-group>
</template>

<script lang="ts">
    import Multiselect from 'vue-multiselect';
    import {SelectParameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import mixins from 'vue-typed-mixins';

    export default mixins(ToolParameterMixin).extend({
        name: 'SelectParameter',
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
                selected: [],
            };
        },
        computed: {
            isMulti(): boolean {
                return this.parameter.maxSelectedOptions > 1;
            },
        },
        watch: {
            selected: {
                immediate: true,
                handler(value: any) { // TODO: can we refine the types some more?
                    this.setSubmissionValue(this.isMulti ? value.map((v: any) => v.value) : value.value);
                },
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
