<template>
    <BaseModal :title="$t('helpModals.titles.updates')"
               @close="$emit('close')">
        <div v-for="(update, index) in updates"
             :key="update.date"
             class="update-item">
            <h6 v-text="moment(update.date).format('LL')"></h6>
            <div v-html="update.html"></div>
            <hr v-if="index < (updates.length - 1)">
        </div>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import updates from '@/i18n/lang/updates';

    export default Vue.extend({
        name: 'Updates',
        components: {
            BaseModal,
        },
        data() {
            return {
                updates,
            };
        },
    });
</script>

<style lang="scss" scoped>
    .update-item {
        h6 {
            color: $primary;
            margin-bottom: 0.25em;
            font-size: 1.1em;
        }
        p {
            color: $tk-darker-gray;
        }
    }
</style>
