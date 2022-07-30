<template>
    <BaseModal id="updates"
               :title="$t('footerLinkModals.titles.updates')">
        <div v-for="(update, index) in updates"
             :key="update.date"
             class="update-item">
            <h6 v-text="formatDate(update.date)"></h6>
            <div v-html="update.html"></div>
            <hr v-if="index < (updates.length - 1)">
        </div>
    </BaseModal>
</template>

<script lang="ts">
import Vue from 'vue';
import BaseModal from './BaseModal.vue';
import updates from '@/i18n/lang/updates';
import {DateTime} from 'luxon';

export default Vue.extend({
    name: 'UpdatesModal',
    components: {
        BaseModal,
    },
    data() {
        return {
            updates,
        };
    },
    methods: {
        formatDate(dateString: string): string {
            return DateTime.fromISO(dateString).toLocaleString(DateTime.DATE_FULL);
        }
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
