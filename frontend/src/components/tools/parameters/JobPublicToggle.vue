<template>
    <i class="tool-action tool-action-push-up fa mr-4"
       :class="[isPublic ? 'fa-lock-open' : 'fa-lock']"
       :title="$t('tools.parameters.isPublic.' + isPublic)"
       @click="togglePublic"></i>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import Switches from 'vue-switches';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {Job} from '@/types/toolkit/jobs';

    export default mixins(ToolParameterMixin).extend({
        name: 'JobPublicToggle',
        components: {Switches},
        props: {
            job: {
                type: Object as () => Job,
                required: false,
                default: undefined,
            },
        },
        computed: {
            parameterName() {
                // override mixin value
                return 'isPublic';
            },
            defaultSubmissionValue(): boolean {
                // if logged in then default is private, else public
                return !this.$store.getters['auth/loggedIn'];
            },
            isPublic(): boolean {
                if (this.job) {
                    return this.job.isPublic;
                } else {
                    return this.submissionValue;
                }
            },
        },
        methods: {
            togglePublic(): void {
                if (this.job) {
                    this.$store.dispatch('jobs/setJobPublic', {jobID: this.job.jobID, isPublic: !this.isPublic});
                } else {
                    this.submissionValue = !this.isPublic;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
