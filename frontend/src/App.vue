<template>
    <div>
        <b-container class="main-container">
            <b-row>
                <Header></Header>
            </b-row>
            <b-row>
                <b-col v-if="showJobList"
                       md="2">
                    <JobList/>
                </b-col>
                <b-col :md="showJobList ? 10 : 12">
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

<style lang="scss" scoped>
    .main-container {
        background-color: $bg-gray;
        box-shadow: 1px 2px 4px 3px rgba(200, 200, 200, 0.75);
        padding: 10px 20px 0 20px;
        margin-bottom: 3rem;
    }
</style>
