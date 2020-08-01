<template>
    <transition
            name="expand"
            @enter="enter"
            @after-enter="afterEnter"
            @leave="leave">
        <slot/>
    </transition>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'ExpandHeight',
        methods: {
            enter(element: HTMLElement): void {
                element.style.width = getComputedStyle(element).width;
                element.style.position = 'absolute';
                element.style.visibility = 'hidden';
                element.style.height = 'auto';

                const height = getComputedStyle(element).height;

                element.style.removeProperty('width');
                element.style.removeProperty('position');
                element.style.removeProperty('visibility');
                element.style.height = '0';

                // Trigger the animation.
                // We use `setTimeout` because we need
                // to make sure the browser has finished
                // painting after setting the `height`
                // to `0` in the line above.
                setTimeout(() => {
                    element.style.height = height;
                });
            },
            afterEnter(element: HTMLElement): void {
                element.style.height = 'auto';
            },
            leave(element: HTMLElement): void {
                element.style.height = getComputedStyle(element).height;

                setTimeout(() => {
                    element.style.height = '0';
                });
            },
        },
    });
</script>

<style lang="scss" scoped>
    .expand-enter-active,
    .expand-leave-active {
        transition: height 0.4s ease-in-out;
        overflow: hidden;
    }

    .expand-enter,
    .expand-leave-to {
        height: 0;
    }

    * {
        will-change: height;
        transform: translateZ(0);
        backface-visibility: hidden;
        perspective: 1000px;
    }
</style>