<template>
    <div>
        <b-container class="main-container">
            <b-row>
                <Header></Header>
            </b-row>
            <b-row class="pt-3 mb-2">
                <b-col v-if="showJobList"
                       md="2">
                    <transition name="expand"
                                @enter="enter"
                                @after-enter="afterEnter"
                                @leave="leave"
                                appear>
                        <SideBar/>
                    </transition>
                </b-col>
                <b-col :md="showJobList ? 10 : 12">
                    <router-view/>
                </b-col>
            </b-row>
            <b-row>
                <Footer></Footer>
            </b-row>
        </b-container>

        <modals-container/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Header from '@/components/header/Header.vue';
    import Footer from '@/components/Footer.vue';
    import SideBar from '@/components/sidebar/SideBar.vue';

    export default Vue.extend({
        name: 'App',
        components: {
            Header,
            SideBar,
            Footer,
        },
        computed: {
            showJobList(): boolean {
                return this.$route.meta.showJobList;
            },
        },
        created() {
            this.$store.dispatch('tools/fetchAllTools');
        },
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
    .main-container {
        background-color: $bg-gray;
        box-shadow: 1px 2px 4px 3px rgba(200, 200, 200, 0.75);
        padding: 10px 1.8rem 0 25px;
        margin-bottom: 3rem;
    }

    .expand-enter-active,
    .expand-leave-active {
        transition: width 0.5s ease-in-out;
        overflow: hidden;
    }

    .expand-enter,
    .expand-leave-to {
        width: 0;
    }

    * {
        will-change: width;
        transform: translateZ(0);
        backface-visibility: hidden;
        perspective: 1000px;
    }
</style>
