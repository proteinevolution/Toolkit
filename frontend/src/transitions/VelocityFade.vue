<template>
    <transition
            name="fade"
            mode="out-in"
            :css="false"
            :before-enter="beforeEnter"
            :enter="enter"
            :before-leave="beforeLeave"
            :leave="leave">
        <slot></slot>
    </transition>
</template>

<script lang="ts">
    import Velocity from 'velocity-animate';
    import Vue from 'vue';

    export default Vue.extend({
        name: 'VelocityFade',
        props: {
            duration: {
                type: Number,
                default: 500,
            },
        },
        methods: {
            beforeEnter(el: HTMLElement): void {
                el.style.opacity = '0';
            },
            enter(el: HTMLElement, done: () => void): void {
                Velocity(el,
                    {opacity: 1},
                    {
                        duration: this.duration,
                        easing: [0.39, 0.67, 0.04, 0.98],
                        complete: () => {
                            done();
                        },
                    },
                );
            },
            beforeLeave(el: HTMLElement): void {
                el.style.opacity = '1';
            },
            leave(el: HTMLElement, done: () => void): void {
                Velocity(el,
                    {opacity: 0},
                    {
                        duration: this.duration,
                        easing: [0.39, 0.67, 0.04, 0.98],
                        complete: () => {
                            done();
                        },
                    },
                );
            },
        },
    });
</script>
