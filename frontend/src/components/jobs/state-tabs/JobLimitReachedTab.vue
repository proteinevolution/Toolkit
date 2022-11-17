<template>
    <div>
        <h5 v-text="$t('jobs.stateMessages.limitReached')"></h5>
        <p v-text="$t('jobs.stateMessages.limitInfo')"></p>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'JobLimitReachedTab',
});
</script>
