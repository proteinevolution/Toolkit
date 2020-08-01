<template>
    <div class="loading-view">
        <img :src="require('../../assets/images/minlogo.svg')" alt="MPI Bioinformatics Toolkit"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'LoadingView',
    });
</script>

<style lang="scss" scoped>
    .loading-view {
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translateX(-50%) translateY(-50%);
    }
</style>
