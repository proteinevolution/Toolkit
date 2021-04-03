<template>
    <div v-if="isAdmin"
         class="admin-view">
        <div class="admin-header">
            <h1>Admin Page</h1>
        </div>

        <b-card>
            <h4>Maintenance Mode</h4>
            <b-button variant="primary"
                      @click="toggleMaintenanceMode"
                      v-text="$t('maintenance.' + (maintenanceMode ? 'end' : 'start'))" />
            <hr>
            <h4>Maintenance Messages</h4>
            <b-form-group>
                <b-textarea v-model="maintenanceMessage"
                            class="mb-2" />
                <b-button variant="primary"
                          class="mr-2"
                          :disabled="messageLoading"
                          @click="setMaintenanceMessage">
                    <loading v-if="messageLoading"
                             message="Set"
                             :size="20" />
                    <span v-else>Set</span>
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

export default hasHTMLTitle.extend({
    name: 'AdminView',
    components: {
        Loading,
    },
    created() {
        this.fetchMaintenanceMessage();
    },
    data() {
        return {
            maintenanceMessage: '',
            messageLoading: false,
        };
    },
    computed: {
        htmlTitle() {
            return 'Admin Page';
        },
        maintenanceMode(): boolean {
            return this.$store.state.maintenanceMode;
        },
        loggedIn(): boolean {
            return this.$store.getters['auth/loggedIn'];
        },
        user(): User | null {
            return this.$store.getters['auth/user'];
        },
        isAdmin(): boolean {
            return this.user !== null && this.user.isAdmin;
        },
    },
    methods: {
        toggleMaintenanceMode(): void {
            if (this.maintenanceMode) {
                backendService.endMaintenance();
            } else {
                backendService.startMaintenance();
            }
        },
        fetchMaintenanceMessage(): void {
            this.messageLoading = true;
            backendService.fetchMaintenanceMessage().then((msg: string) => {
                this.maintenanceMessage = msg;
            }).finally(() => {
                this.messageLoading = false;
            });
        },
        setMaintenanceMessage(): void {
            this.messageLoading = true;
            backendService.setMaintenanceMessage(this.maintenanceMessage).finally(() => {
                this.messageLoading = false;
            });
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
