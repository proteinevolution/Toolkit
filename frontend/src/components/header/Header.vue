<template>
    <b-container class="header">
        <b-row>
            <TopNavBar></TopNavBar>
        </b-row>
        <b-row>
            <b-col cols="3">
                <router-link to="/">
                    <img :src="require('../../assets/images/minlogo.svg')" alt="MPI Bioinformatics Toolkit"/>
                </router-link>
            </b-col>
            <b-col cols="9">
                <NavBar></NavBar>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import NavBar from '@/components/header/NavBar.vue';
    import TopNavBar from '@/components/header/TopNavBar.vue';

    export default Vue.extend({
        name: 'Header',
        components: {
            NavBar,
            TopNavBar,
        },
    });
</script>

<style scoped lang="scss">

</style>
