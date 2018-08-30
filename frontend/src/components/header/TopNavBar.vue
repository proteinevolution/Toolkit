<template>
    <b-col cols="12"
           class="top-navbar">

        <div class="meta-user"></div>
        <div class="social-nav">
            <b-button variant="href"
                      href="https://github.com/proteinevolution/Toolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-github"></i>
            </b-button>
            <b-button variant="href"
                      href="https://www.facebook.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-facebook-f"></i>
            </b-button>
            <b-button variant="href"
                      href="https://twitter.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-twitter"></i>
            </b-button>
            <b-button variant="href"
                      href="#"
                      size="sm"
                      class="sign-in-link">
                Sign In
            </b-button>
        </div>

        <div class="warnings-container">
            <b-alert variant="warning"
                     class="maintenance-alert"
                     fade
                     :show="maintenanceMode">
                <i class="fa fa-wrench"></i>
                <b>Maintenance in a few seconds!</b>
            </b-alert>
            <div class="offline-alert"
                 @click="reload"
                 v-if="reconnecting">
                <i class="fas fa-retweet"></i>
                <b>Reconnecting...</b>
            </div>
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
        methods: {
            reloadApp(): void {
                window.location.reload();
            },
        },
    });
</script>

<style lang="scss" scoped>
    .top-navbar {
        width: 100%;
        display: flex;
        flex-direction: row-reverse;
    }

    .social-nav {
        .dark-link i {
            color: $tk-dark-gray;
        }
    }

    .warnings-container {
        margin-right: 0.5rem;
        display: flex;

        .maintenance-alert, .offline-alert {
            font-size: 0.8em;
            padding: 0.5rem 1rem;

            i {
                margin-right: 0.4rem;
            }
        }

        .offline-alert {
            color: $danger;
            cursor: pointer;
        }
    }

</style>
