<template>
    <BaseModal :title="'Help'"
               @close="$emit('close')">
        <div v-html="contents"/>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';

    export default Vue.extend({
        name: 'HelpModal',
        components: {
            BaseModal,
        },
        props: {
            contents: {
                type: String,
                required: true,
            },
        },
    });
</script>
