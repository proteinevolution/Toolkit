<template>
    <div v-if="isAdmin" class="admin-view">
        <div class="admin-header">
            <h1>Admin Page</h1>
        </div>

        <b-card>
            <h4>Maintenance</h4>
            <b-form-group>
                <b-textarea v-model="maintenance.message" class="mb-2" />
                <b-row class="mb-2">
                    <b-col>
                        <span class="mr-2">Block Submit</span>
                        <switches v-model:model-value="maintenance.submitBlocked" />
                    </b-col>
                </b-row>
                <b-button
                    variant="primary"
                    class="mr-2"
                    :disabled="maintenanceStateLoading"
                    @click="setMaintenanceState">
                    <loading v-if="maintenanceStateLoading" message="Set" :size="20" />
                    <span v-else>Set</span>
                </b-button>
                <b-button
                    variant="primary"
                    class="mr-2"
                    :disabled="maintenanceStateLoading"
                    @click="resetMaintenanceState">
                    <loading v-if="maintenanceStateLoading" message="Reset" :size="20" />
                    <span v-else>Reset</span>
                </b-button>
            </b-form-group>
            <h4>Statistics</h4>
            <admin-statistics />
        </b-card>
    </div>
    <div v-else></div>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';
import { backendService } from '@/services/BackendService';
import Loading from '@/components/utils/Loading.vue';
import Switches from 'vue-switches';
import AdminStatistics from './AdminStatistics.vue';
import { useRootStore } from '@/stores/root';
import { useAuthStore } from '@/stores/auth';
import useToolkitTitle from '@/composables/useToolkitTitle';

useToolkitTitle('Admin Page');

const { maintenance: storedMaintenance } = useRootStore();
let maintenance = reactive({
    message: storedMaintenance.message,
    submitBlocked: storedMaintenance.submitBlocked,
});
const maintenanceStateLoading = ref(false);

const setMaintenanceState = async () => {
    maintenanceStateLoading.value = true;
    await backendService.setMaintenanceState(maintenance);
    maintenanceStateLoading.value = false;
};

const resetMaintenanceState = () => {
    maintenance = {
        message: '',
        submitBlocked: false,
    };
    setMaintenanceState();
};

const { user } = useAuthStore();
const isAdmin = computed<boolean>(() => user?.isAdmin === true);
</script>

<style lang="scss" scoped>
.admin-header {
    height: 2.75rem;

    h1 {
        color: $primary;
        font-weight: bold;
        font-size: 1.25em;
        line-height: 1.6;
    }
}
</style>
