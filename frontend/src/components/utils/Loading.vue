<template>
    <div class="wrapper"
         :class="{wrapperSecondary: variant==='secondary'}">
        <vue-simple-spinner :class="'vue-simple-spinner-' + variant"
                            :speed="variant === 'secondary' ? 2 : 0.8"></vue-simple-spinner>
        <div class="message">{{ message }}</div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import VueSimpleSpinner from 'vue-simple-spinner';

    export default Vue.extend({
        name: 'Loading',
        props: {
            variant: {
                type: String,
                required: false,
                default: 'primary',
            },
            message: {
                type: String,
                required: false,
                default: '',
            },
        },
        components: {
            VueSimpleSpinner,
        },
    });
</script>

<style lang="scss" scoped>
    .message {
        margin-left: 0.5em;
    }

    .wrapper {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .wrapperSecondary {
        opacity: 0.6;
    }
</style>

<style lang="scss">
    .vue-simple-spinner-primary .vue-simple-spinner {
        // "important" is needed here to override the 'style'-property in vue-simple-spinner
        border-top-color: $primary !important;
        border-bottom-color: $tk-lighter-gray !important;
        border-right-color: $tk-lighter-gray !important;
        border-left-color: $tk-lighter-gray !important;
    }

    .vue-simple-spinner-secondary .vue-simple-spinner {
        // "important" is needed here to override the 'style'-property in vue-simple-spinner
        border-top-color: $tk-gray !important;
        border-bottom-color: $tk-lighter-gray !important;
        border-right-color: $tk-lighter-gray !important;
        border-left-color: $tk-lighter-gray !important;
    }
</style>


