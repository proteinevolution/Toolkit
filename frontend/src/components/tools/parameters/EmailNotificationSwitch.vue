<template>
    <div class="d-flex">
        <switches v-model="submissionValue"
                  :label="$t('tools.parameters.emailNotification')"/>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import Switches from 'vue-switches';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';

    export default mixins(ToolParameterMixin).extend({
        name: 'EmailNotificationSwitch',
        components: {Switches},
        computed: {
            parameterName() {
                // override mixin value
                return 'emailUpdate';
            },
            defaultSubmissionValue(): boolean {
                return false;
            },
        },
        methods: {
            submissionValueFromString(value: string): boolean {
                return value === 'true';
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
