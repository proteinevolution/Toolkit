<template>
    <div v-if="isAdmin"
         class="admin-view">
        <div class="admin-header">
            <h1>Admin Page</h1>
        </div>

        <b-card>
            <h4>Maintenance</h4>
            <b-form-group>
                <b-textarea v-model="maintenance.message"
                            class="mb-2" />
                <b-row class="mb-2">
                    <b-col>
                        <span class="mr-2">Block Submit</span>
                        <switches v-model="maintenance.submitBlocked" />
                    </b-col>
                </b-row>
                <b-button variant="primary"
                          class="mr-2"
                          :disabled="maintenanceStateLoading"
                          @click="setMaintenanceState">
                    <loading v-if="maintenanceStateLoading"
                             message="Set"
                             :size="20" />
                    <span v-else>Set</span>
                </b-button>
                <b-button variant="primary"
                          class="mr-2"
                          :disabled="maintenanceStateLoading"
                          @click="resetMaintenanceState">
                    <loading v-if="maintenanceStateLoading"
                             message="Reset"
                             :size="20" />
                    <span v-else>Reset</span>
                </b-button>
            </b-form-group>
        </b-card>
    </div>
    <div v-else></div>
</template>

<script lang="ts">
import hasHTMLTitle from '@/mixins/hasHTMLTitle';
import {backendService} from '@/services/BackendService';
import {User} from '@/types/toolkit/auth';
import Loading from '@/components/utils/Loading.vue';
import Switches from 'vue-switches';
import {useRootStore} from '@/stores/root';
import {mapStores} from 'pinia';
import {useAuthStore} from '@/stores/auth';

export default hasHTMLTitle.extend({
    name: 'AdminView',
    components: {
        Loading,
        Switches,
    },
    data() {
        return {
            maintenance: {
              message: this.rootStore.maintenance.message,
              submitBlocked: this.rootStore.maintenance.submitBlocked,
            },
            maintenanceStateLoading: false,
        };
    },
    computed: {
        htmlTitle() {
            return 'Admin Page';
        },
        loggedIn(): boolean {
            return this.authStore.loggedIn;
        },
        user(): User | null {
            return this.authStore.user;
        },
        isAdmin(): boolean {
            return this.user !== null && this.user.isAdmin;
        },
        ...mapStores(useRootStore, useAuthStore),
    },
    methods: {
        setMaintenanceState(): void {
            this.maintenanceStateLoading = true;
            backendService.setMaintenanceState(this.maintenance).finally(() => {
                this.maintenanceStateLoading = false;
            });
        },
        resetMaintenanceState(): void {
            this.maintenance.message = '';
            this.maintenance.submitBlocked = false;
            this.setMaintenanceState();
        },
    },
});
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
