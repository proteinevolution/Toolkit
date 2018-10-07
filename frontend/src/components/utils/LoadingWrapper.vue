<template>
    <VelocityFade>
        <Loading v-if="loading"></Loading>
        <slot v-else></slot>
    </VelocityFade>
</template>

<script lang="ts">
    import Vue from 'vue';
    import VelocityFade from '../../transitions/VelocityFade.vue';
    import Loading from './Loading.vue';

    export default Vue.extend({
        name: 'LoadingWrapper',
        props: {
            loading: {
                type: Boolean,
                required: false,
                default: true,
            },
        },
        components: {
            VelocityFade,
            Loading,
        },
    });
</script>

<style lang="scss" scoped>
</style>