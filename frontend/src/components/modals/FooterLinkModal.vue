<template>
    <BaseModal id="footerLink" :title="$t('footerLinkModals.titles.' + modal)">
        <div v-html="$t('footerLinkModals.content.' + modal)"></div>
    </BaseModal>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BaseModal from './BaseModal.vue';

export default defineComponent({
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
