<template>
    <b-col cols="12"
           class="top-navbar">
        <div class="float-right meta-user"></div>
        <div class="float-right social-nav">
            <b-button variant="href" href="https://github.com/proteinevolution/Toolkit" target="_blank" rel="noopener"
                      class="">
                <i class="fab fa-github"></i></b-button>
            <b-button variant="href" href="https://www.facebook.com/mpitoolkit" target="_blank" rel="noopener"><i
                    class="fab fa-facebook-f"></i></b-button>
            <b-button variant="href" href="https://twitter.com/mpitoolkit" target="_blank" rel="noopener"><i
                    class="fab fa-twitter"></i></b-button>
            <b-button variant="href" href="#">Sign In</b-button>
        </div>

        <div class="maintenance-alert float-right"
             v-if="maintenanceMode">
            <i class="fa fa-wrench"></i>
            <b>Maintenance in a few seconds!</b>
        </div>
        <div id="offline-alert"
             class="float-right"
             onclick="window.location.reload()"
             v-if="reconnecting">
            <i class="fas fa-retweet"></i>
            <b>Reconnecting...</b>
        </div>
    </b-col>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'TopNavBar',
        computed: {
            maintenanceMode(): boolean {
                return this.$store.state.maintenanceMode;
            },
            reconnecting(): boolean {
                return this.$store.state.reconnecting;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .social-nav {

    }

    .top-navbar {
        width: 100%
    }
</style>
