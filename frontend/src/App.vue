<template>
    <div class="toolkit">
        <vue-particles class="tk-particles"
                       color="#d0d0d0"
                       :particleOpacity="0.7"
                       :particlesNumber="80"
                       shapeType="circle"
                       :particleSize="4"
                       linesColor="#ccc"
                       :linesWidth="1"
                       :lineLinked="true"
                       :lineOpacity="0.4"
                       :linesDistance="150"
                       :moveSpeed="2"
                       :hoverEffect="true"
                       hoverMode="grab"
                       :clickEffect="true"
                       clickMode="push"/>

        <VelocityFade>
            <LoadingView v-if="$store.state.loading.tools">
            </LoadingView>
            <b-container v-else class="main-container">
                <b-row>
                    <Header></Header>
                </b-row>
                <b-row class="pt-3 mb-2 main-content"
                       :class="[showJobList ? 'job-list-visible' : '']">
                    <b-col class="job-list-col"
                           md="2">
                        <SideBar/>
                    </b-col>
                    <b-col>
                        <VelocityFade :duration="1000">
                            <router-view/>
                        </VelocityFade>
                    </b-col>
                </b-row>
                <b-row>
                    <Footer></Footer>
                </b-row>
            </b-container>
        </VelocityFade>

        <modals-container/>
        <notifications animation-type="velocity"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Header from '@/components/header/Header.vue';
    import Footer from '@/components/Footer.vue';
    import SideBar from '@/components/sidebar/SideBar.vue';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import LoadingView from '@/components/utils/LoadingView.vue';

    export default Vue.extend({
        name: 'App',
        components: {
            Header,
            SideBar,
            Footer,
            VelocityFade,
            LoadingView,
        },
        computed: {
            showJobList(): boolean {
                return this.$route.meta.showJobList;
            },
        },
        created() {
            this.$store.dispatch('tools/fetchAllTools');
        },
    });
</script>

<style lang="scss" scoped>
    .main-container {
        background-color: $bg-gray;
        box-shadow: 1px 2px 4px 3px rgba(200, 200, 200, 0.75);
        padding: 10px 1.8rem 0 25px;
        margin-bottom: 3rem;
        border-bottom-left-radius: $global-radius;
        border-bottom-right-radius: $global-radius;
        z-index: 1;
        position: relative;
    }

    .main-content .job-list-col {
        transition: padding 0.6s, opacity 0.6s, max-width 0.6s;
    }

    .main-content:not(.job-list-visible) .job-list-col {
        max-width: 0;
        padding: 0;
        opacity: 0;
    }

    .tk-particles {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .fetching-spinner {
        position: fixed;
        bottom: 1em;
        left: 1em;
    }
</style>

<style lang="scss">
    .toolkit .vue-notification {
        padding: 10px;
        margin: 5px 10px 0;
        border-radius: $global-radius;

        font-size: 12px;

        color: #ffffff;
        background: $primary-light;
        border-left: 5px solid $primary;

        &.warning, &.warn {
            background: $warning-light;
            border-left-color: $warning;
        }

        &.danger, &.error {
            background: $danger-light;
            border-left-color: $danger;
        }
    }
</style>
