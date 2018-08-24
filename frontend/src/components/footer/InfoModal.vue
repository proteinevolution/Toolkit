<template>
    <!-- Generic template for all footer modals - content (arbitrary html) is defined in extra components -->
    <b-modal v-bind:id="id" v-bind:title="title" hide-footer>
        <slot></slot>
    </b-modal>
</template>

<script>
    import Vue from 'vue';

    export default Vue.extend({
        name: 'Modal',
        props: {
            id: String,
            title: String,
        }
    });
</script>
