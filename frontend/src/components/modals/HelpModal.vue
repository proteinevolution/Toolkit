<template>
    <BaseModal :title="toolName"
               @close="$emit('close')">
        <component :is="`${toolName}Help`"></component>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';

    export default Vue.extend({
        name: 'HelpModal',
        components: {
            BaseModal,
            hhblitsHelp: () => import('./help/hhblitsHelp.vue'),
        },
        props: {
            toolName: {
                type: String,
                required: true,
            },
        },
    });
</script>

<style>
    .tab-pane {
        padding: 2em;
    }
</style>
