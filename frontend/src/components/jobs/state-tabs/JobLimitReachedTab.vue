<template>
    <div>
        <h5 v-html="$t('jobs.stateMessages.limitReached')"></h5>
        <p v-html="$t('jobs.stateMessages.limitInfo')"></p>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
  name: 'JobLimitReachedTab',
});
</script>
