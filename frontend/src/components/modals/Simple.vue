<template>
    <BaseModal :title="$t('helpModals.titles.' + modal)"
               id="simple">
        <div v-html="$t('helpModals.content.' + modal)"/>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';

    export default Vue.extend({
        name: 'Simple',
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
