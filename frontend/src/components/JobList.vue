<template>
    <div id="jobs">
        <SearchField class="form-control-sm form-control-gray"></SearchField>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import SearchField from '@/components/index/SearchField.vue';

    export default Vue.extend({
        name: 'JobList',
        components: {
            SearchField,
        },
    });
</script>

<style lang="scss" scoped>
    .form-control-gray:not(:focus) {
        background-color: transparent;
    }
</style>
