<template>
    <b-container class="footer">
        <b-btn variant="link" size="sm" @click="launchHelpModal">{{ $t('helpTitle') }}</b-btn>
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
        },
        methods: {
            launchHelpModal() {
                this.$modal.show(Help, {}, {
                    draggable: true,
                });
            },
        },
    });
</script>

<style scoped lang="scss">
    .footer {
        background-color: #dddddd;
    }
</style>
