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
            <template #maxElements>{{ $t('tools.parameters.select.maxElementsSelected') }}</template>
        </multiselect>

    </b-form-group>
</template>

<script lang="ts">
    import Multiselect from 'vue-multiselect';
    import {SelectOption, SelectParameter} from '@/types/toolkit/tools';
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
        computed: {
            defaultSubmissionValue(): any {
                // overrides the property in ToolParameterMixin
                return this.parameter.default || '';
            },
            selected: {
                get(): SelectOption[] {
                    if (this.isMulti) {
                        // submissionValue contains the selected option values separated by whitespaces in this case
                        return this.parameter.options.filter((o: SelectOption) => this.submissionValue.includes(o.value));
                    } else {
                        return this.parameter.options.filter((o: SelectOption) => o.value === this.submissionValue);
                    }
                },
                set(value: SelectOption[] | SelectOption) {
                    this.submissionValue = value instanceof Array ?
                        value.map((o: SelectOption) => o.value).join(' ') :
                        value.value;
                },
            },
            isMulti(): boolean {
                return this.parameter.maxSelectedOptions > 1;
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
