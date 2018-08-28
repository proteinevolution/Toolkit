<template>
    <div>
        <b-container>
            <b-row>
                <Header></Header>
            </b-row>
            <b-row>
                <b-col v-if="showJobList"
                       cols="3">
                    <JobList/>
                </b-col>
                <b-col :cols="showJobList ? 9 : 12">
                    <router-view/>
                </b-col>
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
    import JobList from '@/components/JobList.vue';

    export default Vue.extend({
        name: 'App',
        components: {
            Header,
            JobList,
            Footer,
        },
        computed: {
            showJobList(): boolean {
                return this.$route.meta.showJobList;
            },
        },
        created() {
            this.$store.dispatch('tools/fetchAllTools');
        },
    });
</script>

<style lang="scss">

</style>
