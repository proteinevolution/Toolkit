<template>
    <div v-show="display" ref="scrollTop" class="scroll-top-button" @click="scrollTop"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue';
import { throttle } from 'lodash-es';

const display = ref(false);

function handleScroll(scrollThreshold = 200): void {
    display.value = window.pageYOffset > scrollThreshold;
}

const throttleScroll = throttle(handleScroll, 300) as () => void;
throttleScroll();

window.addEventListener('scroll', throttleScroll);
onBeforeUnmount(() => {
    window.removeEventListener('scroll', throttleScroll);
});

function scrollTop(): void {
    window.scrollTo({
        top: 0,
        left: 0,
        behavior: 'smooth',
    });
}
</script>

<style lang="scss" scoped>
.scroll-top-button {
    width: 40px;
    height: 40px;
    position: fixed;
    cursor: pointer;
    bottom: 2rem;
    right: 2rem;
    background: url(../../assets/images/arrow-top.svg) no-repeat;
    background-size: 40px 40px;
    outline: 0;
    z-index: 10;
}
</style>
