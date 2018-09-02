<template>
    <transition
            name="expand-width"
            @enter="enter"
            @after-enter="afterEnter"
            @leave="leave">
        <slot/>
    </transition>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'ExpandWidth',
        methods: {
            enter(element: HTMLElement): void {
                element.style.height = getComputedStyle(element).height;
                element.style.position = 'absolute';
                element.style.visibility = 'hidden';
                element.style.width = 'auto';

                const width = getComputedStyle(element).width;

                element.style.height = null;
                element.style.position = null;
                element.style.visibility = null;
                element.style.width = '0';

                // Trigger the animation.
                // We use `setTimeout` because we need
                // to make sure the browser has finished
                // painting after setting the `height`
                // to `0` in the line above.
                setTimeout(() => {
                    element.style.width = width;
                });
            },
            afterEnter(element: HTMLElement): void {
                element.style.width = 'auto';
            },
            leave(element: HTMLElement): void {
                element.style.width = getComputedStyle(element).width;

                setTimeout(() => {
                    element.style.width = '0';
                });
            },
        },
    });
</script>

<style lang="scss" scoped>
    .expand-width-enter-active,
    .expand-width-leave-active {
        transition: width 0.5s ease-in-out;
        overflow: hidden;
    }

    .expand-width-enter,
    .expand-width-leave-to {
        width: 0;
    }

    * {
        will-change: width;
        transform: translateZ(0);
        backface-visibility: hidden;
        perspective: 1000px;
    }
</style>