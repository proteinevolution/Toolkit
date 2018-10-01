<template>
    <VelocityFade>
        <vue-simple-spinner v-if="loading"></vue-simple-spinner>
        <slot v-else></slot>
    </VelocityFade>
</template>

<script lang="ts">
    import Vue from 'vue';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import VueSimpleSpinner from 'vue-simple-spinner';

    export default Vue.extend({
        name: 'Loading',
        props: {
            loading: Boolean,
        },
        components: {
            VelocityFade,
            VueSimpleSpinner,
        },
    });
</script>

<style lang="scss" scoped>
</style>
