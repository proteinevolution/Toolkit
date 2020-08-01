<template>
    <BaseModal :title="$t('footerLinkModals.titles.' + modal)"
               id="footerLink">
        <div v-html="$t('footerLinkModals.content.' + modal)"/>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';

    export default Vue.extend({
        name: 'FooterLinkModal',
        components: {
            BaseModal,
        },
        props: {
            modal: {
                type: String,
                required: true,
            },
        },
    });
</script>
