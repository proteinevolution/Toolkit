<template>
    <div class="home">
        <h1>{{ $t("welcome") }}</h1>
        <router-link to="/jobmanager">Job Manager</router-link>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";

    export default Vue.extend({
        name: "HomeView",
    });
</script>
