<template>
    <b-container id="header">
        <b-row>
            <TopNavBar></TopNavBar>
        </b-row>
        <b-row>
            <b-col cols="3">MPI Bioinformatics Toolkit (Logo)</b-col>
            <b-col>
                <div id="nav">
                    <router-link to="/">Home</router-link>
                    -
                    <router-link to="/tools/test">Tool Test</router-link>
                </div>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import TopNavBar from '@/components/header/TopNavBar.vue';

    export default Vue.extend({
        name: 'Header',
        components: {
            TopNavBar,
        },
    });
</script>

<style scoped lang="scss">
    #header {
        background-color: #dddddd;
    }
</style>
