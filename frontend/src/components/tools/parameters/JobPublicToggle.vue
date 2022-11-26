<template>
    <i
        class="tool-action tool-action-push-up fa mr-4 hover-unlock"
        :class="[isPublic ? 'fa-lock-open' : 'fa-lock']"
        :title="t('tools.parameters.isPublic.' + isPublic)"
        @click="togglePublic"></i>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { SimpleToolParameterProps, useSimpleToolParameter } from '@/composables/useToolParameter';
import { Job } from '@/types/toolkit/jobs';
import { useJobsStore } from '@/stores/jobs';
import { useAuthStore } from '@/stores/auth';
import { useI18n } from 'vue-i18n';
import { isNonNullable } from '@/util/nullability-helpers';

const { t } = useI18n();
const authStore = useAuthStore();
const jobsStore = useJobsStore();

interface JobPublicToggleProps extends SimpleToolParameterProps {
    submission: Record<string, any>;
    // Custom props
    job?: Job;
}

const props = defineProps<JobPublicToggleProps>();

const parameterName = ref('isPublic');
// default is private if logged in else public
const defaultSubmissionValue = computed(() => !authStore.loggedIn);

const { submissionValue } = useSimpleToolParameter({
    props,
    parameterName,
    defaultSubmissionValue,
    submissionValueFromString: (value: string): boolean => value === 'true',
});

const isPublic = computed(() => {
    if (isNonNullable(props.job)) {
        return props.job.isPublic;
    } else {
        return submissionValue;
    }
});

function togglePublic(): void {
    if (isNonNullable(props.job)) {
        jobsStore.setJobPublic(props.job.jobID, !isPublic.value);
    } else {
        submissionValue.value = !isPublic.value;
    }
}
</script>

<style lang="scss" scoped>
.hover-unlock.fa-lock:hover::before {
    content: '\f09c';
}
</style>
