<template>
    <b-container class="header">
        <b-row>
            <TopNavBar></TopNavBar>
        </b-row>
        <b-row>
            <b-col md="2"
                   class="logo-container">
                <router-link to="/"
                             class="logo-link">
                    <img :src="require('../../assets/images/minlogo.svg')" alt="MPI Bioinformatics Toolkit"/>
                </router-link>
            </b-col>
            <b-col md="10">
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

<style lang="scss" scoped>
    .logo-container {
        display: flex;
        align-items: center;
    }

    .logo-link {
        width: 100%;
        img {
            height: auto;
            width: 100%;
        }
    }
</style>
