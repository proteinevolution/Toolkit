<template>
    <b-container class="footer">
        <b-btn variant="link" size="sm" v-b-modal="'helpModal'">{{ $t('helpTitle') }}</b-btn>

        <InfoModal id="helpModal" v-bind:title="$t('helpTitle')">
            <Help></Help>
        </InfoModal>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import InfoModal from '@/components/footer/InfoModal.vue';
    import Help from '@/components/footer/Help.vue';

    export default Vue.extend({
        name: 'Footer',
        components: {
            InfoModal,
            Help,
        },
    });
</script>

<style scoped lang="scss">
    .footer {
        background-color: #dddddd;
    }
</style>
