<template>
    <div class="observer"/>
</template>

<script lang="ts">
    import Vue from 'vue';
    import IntersectionObserver from 'intersection-observer-polyfill';

    export default Vue.extend({
        name: 'IntersectionObserver',
        props: {
            options: {
                type: Object,
                required: false,
                default: () => ({}),
            },
        },
        data() {
            return {
                observer: null as any,
            };
        },
        mounted() {
            this.observer = new IntersectionObserver(([entry]: any) => {
                if (entry && entry.isIntersecting) {
                    this.$emit('intersect');
                }
            }, this.options);

            this.observer.observe(this.$el);
        },
        destroyed() {
            this.observer.disconnect();
        },
    });
</script>
