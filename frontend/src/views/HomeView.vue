<template>
    <div class="home">
        <img :src="require('../assets/images/Toolkit100.png')"/>
        <router-link to="/jobmanager">Job Manager</router-link>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'HomeView',
    });
</script>
