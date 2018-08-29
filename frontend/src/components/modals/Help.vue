<template>
    <BaseModal :title="$t('helpModals.titles.help')">
        {{ $t('helpContents') }}
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';

    export default Vue.extend({
        name: 'Help',
        components: {
            BaseModal,
        },
    });
</script>
