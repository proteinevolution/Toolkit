<template>
    <div class="side-bar">
        <SearchField target-class="form-control-sm form-control-gray" />
        <JobList />
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
.side-bar {
    position: sticky;
    top: 1rem;
}
</style>
