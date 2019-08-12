<template>
    <div class="mb-0 mt-2 d-flex align-items-baseline">
        <switches v-model="submissionValue"
                  class="mr-2"/>
        <div v-text="$t('tools.parameters.emailNotification')"
             class="mb-2"></div>
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
    });
</script>

<style lang="scss" scoped>

</style>
