<template>
    <div ref="scrollTop"
         class="scroll-top-button"
         v-show="display"
         @click="scrollTop"></div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {throttle} from 'lodash-es';

    export default Vue.extend({
        name: 'ScrollTopButton',
        data() {
            return {
                display: false,
                scrollThreshold: 200,
                throttleDelay: 100,
                throttleScroll: undefined as any,
            };
        },
        created() {
            this.throttleScroll = throttle(this.handleScroll, this.throttleDelay);
            window.addEventListener('scroll', this.throttleScroll);
            this.throttleScroll();
        },
        beforeDestroy() {
            if (this.throttleScroll) {
                window.removeEventListener('scroll', this.throttleScroll);
            }
        },
        methods: {
            handleScroll(): void {
                this.display = window.pageYOffset > this.scrollThreshold;
            },
            scrollTop(): void {
                window.scrollTo({
                    top: 0,
                    left: 0,
                    behavior: 'smooth',
                });
            },
        },
    });
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
