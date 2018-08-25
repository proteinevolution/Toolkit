<template>
    <div>
        <b-container>
            <b-row>
                <Header></Header>
            </b-row>
            <b-row>
                <router-view/>
            </b-row>
            <b-row>
                <Footer></Footer>
            </b-row>
        </b-container>

        <modals-container/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Header from '@/components/header/Header.vue';
    import Footer from '@/components/footer/Footer.vue';

    export default Vue.extend({
        name: 'App',
        components: {
            Header,
            Footer,
        },
    });
</script>

<style lang="scss">

</style>
