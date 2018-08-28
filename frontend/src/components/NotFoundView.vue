<template>
    <b-container>
        <b-row>
            <b-col>
                <span class="page-not-found">Page not found.</span>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'NotFoundView',
    });
</script>

<style scoped>
    .page-not-found {
        text-align: center;
    }
</style>