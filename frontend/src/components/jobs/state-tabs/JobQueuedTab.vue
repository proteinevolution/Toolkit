<template>
    <div>
        <h3 class="mb-3 h5"
            v-text="$t('jobs.stateMessages.queued')"></h3>
        <p v-html="$t('jobs.citationInfo', {tool: tool.longname})"></p>
        <div v-text="$t('jobs.jobIDDetails', job)"></div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'JobQueuedTab',
        props: {
            job: {
                type: Object,
                required: true,
            },
            tool: {
                type: Object,
                required: true,
            },
        },
    });
</script>
