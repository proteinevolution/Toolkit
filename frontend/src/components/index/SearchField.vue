<template>
    <input class="form-control search-field"/>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'SearchField',
    });
</script>

<style lang="scss" scoped>
    .search-field {
        font-size: 0.9em;
    }
</style>