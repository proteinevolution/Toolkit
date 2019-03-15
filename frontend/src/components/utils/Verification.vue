<template>
    <div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'Verification',
        watch: {
            // Use a watcher here - component cannot use 'beforeRouteEnter' because of lazy loading
            $route: {
                immediate: true,
                async handler(to: any) {
                    const msg = await AuthService.verifyToken(to.params.nameLogin, to.params.token);
                    EventBus.$emit('show-modal', {id: 'verification', props: {authMessage: msg}});
                },
            },
        },
    });
</script>

<style scoped>

</style>