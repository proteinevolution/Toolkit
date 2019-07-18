<template>
    <div class="wrapper"
         :class="['justify-content-' + justify, variant==='secondary' ? 'wrapper-secondary' : '']">
        <vue-simple-spinner :class="'vue-simple-spinner-' + variant"
                            :speed="variant === 'secondary' ? 2 : 0.8"
                            :size="size"></vue-simple-spinner>
        <div v-if="message"
             class="message">{{ message }}
        </div>
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
            size: {
                type: Number,
                required: false,
                default: 32,
            },
            justify: {
                type: String,
                required: false,
                default: 'left',
            },
        },
        components: {
            VueSimpleSpinner,
        },
    });
</script>

<style lang="scss" scoped>
    .message {
        margin-left: 1em;
    }

    .wrapper {
        display: flex;
        align-items: center;
    }

    .wrapper-secondary {
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


