<template>
    <div class="side-bar">
        <SearchField class="form-control-sm form-control-gray"></SearchField>
        <JobList/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import SearchField from '../index/SearchField.vue';
    import JobList from './JobList.vue';

    export default Vue.extend({
        name: 'SideBar',
        components: {
            SearchField,
            JobList,
        },
    });
</script>

<style lang="scss" scoped>
    .form-control-gray:not(:focus) {
        background-color: transparent;
    }
</style>
