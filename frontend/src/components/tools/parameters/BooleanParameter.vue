<template>
    <b-form-group :label="$t('tools.parameters.labels.' + parameter.name)">
        <switches v-model="submissionValue" />
    </b-form-group>
</template>

<script lang="ts">
import Switches from 'vue-switches';
import { BooleanParameter } from '@/types/toolkit/tools';
import ParameterRememberMixin from '@/mixins/ParameterRememberMixin';

export default ParameterRememberMixin.extend({
    name: 'BooleanParameter',
    components: {
        Switches,
    },
    props: {
        /*
         Simply stating the interface type doesn't work, this is a workaround. See
         https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
         */
        parameter: Object as () => BooleanParameter,
        // this prop can be used by parent components to override the selection, but is not required
        enabledOverride: Boolean,
    },
    computed: {
        defaultSubmissionValue(): any {
            // overrides the property in ToolParameterMixin
            return this.enabledOverride || this.parameter.default;
        },
    },
    watch: {
        enabledOverride() {
            this.submissionValue = this.enabledOverride || this.submissionValue;
        },
    },
    methods: {
        submissionValueFromString(value: string): boolean {
            return value === 'true';
        },
    },
});
</script>
