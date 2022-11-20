<template>
    <transition
        name="fade"
        mode="out-in"
        :before-enter="beforeEnter"
        :enter="enter"
        :before-leave="beforeLeave"
        :leave="leave">
        <slot></slot>
    </transition>
</template>

<script setup lang="ts">
import Velocity from 'velocity-animate';

const props = defineProps({
    duration: {
        type: Number,
        default: 500,
    },
});

function beforeEnter(el: HTMLElement): void {
    el.style.opacity = '0';
}

function enter(el: HTMLElement, done: () => void): void {
    Velocity(
        el,
        { opacity: 1 },
        {
            duration: props.duration,
            easing: [0.39, 0.67, 0.04, 0.98],
            complete: done,
        }
    );
}

function beforeLeave(el: HTMLElement): void {
    el.style.opacity = '1';
}

function leave(el: HTMLElement, done: () => void): void {
    Velocity(
        el,
        { opacity: 0 },
        {
            duration: props.duration,
            easing: [0.39, 0.67, 0.04, 0.98],
            complete: done,
        }
    );
}
</script>
