<template>
    <div v-if="maintenanceMessage.length > 0"
         class="d-flex my-md-4 justify-content-center">
        <b-alert variant="warning"
                 :show="true"
                 class="mb-0"
                 v-html="maintenanceMessage" />
    </div>
    <div v-else
         class="my-md-5"></div>
</template>

<script lang="ts">
import Vue from 'vue';
import {backendService} from '@/services/BackendService';

export default Vue.extend({
    name: 'MaintenanceMessage',
    data() {
        return {
            maintenanceMessage: '',
        };
    },
    created() {
        this.fetchMaintenanceMessage();
    },
    methods: {
        fetchMaintenanceMessage(): void {
            backendService.fetchMaintenanceMessage().then((msg: string) => {
                this.maintenanceMessage = msg;
            });
        },
    },
});
</script>

<style lang="scss" scoped>
</style>
