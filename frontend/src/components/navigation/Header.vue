<template>
    <b-container class="header">
        <b-alert variant="primary"
                 class="tour-banner"
                 :show="showTourBanner">
            <p class="mb-2">
                {{ this.$t('tour.banner.message') }}
            </p>
            <div>
                <button class="tour-banner__ignore-button"
                        @click="ignoreTour">
                    {{ this.$t('tour.banner.ignore') }}
                </button>
                <button class="tour-banner__start-button"
                        @click="startTour">
                    {{ this.$t('tour.banner.start') }}
                </button>
            </div>
        </b-alert>
        <b-row>
            <TopNavBar />
        </b-row>
        <b-row>
            <b-col sm="12"
                   lg="3"
                   xl="2"
                   class="logo-container d-none d-lg-flex">
                <router-link to="/"
                             class="logo-link">
                    <img :src="require('../../assets/images/minlogo.svg')"
                         alt="MPI Bioinformatics Toolkit">
                </router-link>
            </b-col>
            <b-col cols="12"
                   lg="9"
                   xl="10"
                   class="d-none d-lg-flex">
                <NavBar />
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
import Vue from 'vue';
import NavBar from '@/components/navigation/NavBar.vue';
import TopNavBar from '@/components/navigation/TopNavBar.vue';
import {useRootStore} from '@/stores/root';
import {mapStores} from 'pinia';

export default Vue.extend({
    name: 'Header',
    components: {
        NavBar,
        TopNavBar,
    },
    data() {
        return {
            showingTour: false,
        };
    },
    computed: {
        showTourBanner(): boolean {
            return !this.showingTour && !this.rootStore.tourFinished;
        },
        ...mapStores(useRootStore),
    },
    methods: {
        ignoreTour(): void {
            this.rootStore.tourFinished = true;
        },
        startTour(): void {
            this.showingTour = true;
            setTimeout(() => {
                this.$tours['toolkitTour'].start();
            }, 300);
        },
    },
});
</script>

<style lang="scss" scoped>
.logo-container {
    align-items: center;
}

.logo-link {
    width: 100%;

    img {
        height: auto;
        width: 180px;
    }
}

.tour-banner {
    display: flex;
    background-color: $tk-green;
    justify-content: space-between;
    align-items: baseline;
    @include media-breakpoint-down(md) {
        display: none;
    }

    p {
        color: $white;
    }

}

.tour-banner__ignore-button {
    color: $white;
    background-color: transparent;
    padding: 0.625em 3.125em;
    border-radius: $global-radius;
    border: 0;
    font-size: 1em;
    margin: 0;
}

.tour-banner__start-button {
    color: $white;
    background-color: $tk-dark-green;
    padding: 0.625em 3.125em;
    border-radius: $global-radius;
    border: 0;
    font-size: 1em;
    margin: 0;
}

.tour-banner__start-button:hover {
    background-color: $tk-darker-green;
}


</style>
