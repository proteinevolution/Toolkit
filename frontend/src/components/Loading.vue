<template>
    <div class="loading-base">
        <VelocityFade>
            <vue-simple-spinner v-show="loading"
                                class="spinner"
                                line-fg-color="#2E8C81"> <!-- TODO use scss primary color -->
            </vue-simple-spinner>
        </VelocityFade>
        <VelocityFade>
            <slot v-show="!loading"></slot>
        </VelocityFade>
    </div>
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
            VueSimpleSpinner
        }
    });
</script>

<style lang="scss" scoped>
    .loading-base {
        width: 100%;
        height: 100%;
    }

    .spinner {
        position: absolute;
        top: 40%;
        left: 40%;
    }

</style>
