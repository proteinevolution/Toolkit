<template>
    <div class="jobmanager-view">
        <div class="job-manager-header">
            <h1>Job Manager</h1>
        </div>

        <b-card>
            Coming soon...
        </b-card>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'JobManagerView',
    });
</script>

<style lang="scss" scoped>
    .job-manager-header {
        height: 2.75rem;

        h1 {
            color: $primary;
            font-weight: bold;
            font-size: 1.25em;
            line-height: 1.6;
        }
    }
</style>
