<template>
    <b-alert variant="warning" class="maintenance-alert mb-0" fade :show="showMaintenance">
        <i class="fa fa-wrench"></i>
        <b v-text="maintenanceMessage || $t('maintenance.headerWarning')"></b>
    </b-alert>
</template>

<script lang="ts">
import Vue from 'vue';
import { mapStores } from 'pinia';
import { useRootStore } from '@/stores/root';

export default Vue.extend({
    name: 'MaintenanceMessage',
    computed: {
        maintenanceMessage(): string {
            return this.rootStore.maintenance.message;
        },
        showMaintenance(): boolean {
            return this.maintenanceMessage.length > 0;
        },
        ...mapStores(useRootStore),
    },
});
</script>

<style lang="scss" scoped>
.maintenance-alert {
    font-size: 0.8em;
    padding: 0.5rem 1rem;

    i {
        margin-right: 0.4rem;
    }
}
</style>
