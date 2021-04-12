<template>
    <b-container class="header">
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

export default Vue.extend({
    name: 'Header',
    components: {
        NavBar,
        TopNavBar,
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
</style>
