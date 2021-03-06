import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import jobs from './modules/jobs';
import auth from './modules/auth';
import {RootState} from './types';
import localStoragePlugin from './plugins/localStoragePlugin';
import Logger from 'js-logger';
import {backendService} from '@/services/BackendService';
import {MaintenanceState} from '@/types/toolkit/auth';

Vue.use(Vuex);
const logger = Logger.get('Store');

const store: StoreOptions<RootState> = {
    strict: process.env.NODE_ENV !== 'production',
    state: {
        loading: {
            maintenanceState: false,
            tools: false,
            toolParameters: false,
            alignmentTextarea: false,
            login: false,
            logout: false,
        },
        offscreenMenuShow: false,
        maintenance: {
            message: '',
            submitBlocked: false,
        },
        reconnecting: true,
        clusterWorkload: 0,
        // allow for update of human readable time by updating reference point
        now: Date.now(),
    },
    actions: {
        async fetchMaintenance(context) {
            context.commit('startLoading', 'maintenanceState');
            const maintenanceState = await backendService.fetchMaintenanceState();
            context.commit('setMaintenance', maintenanceState);
            context.commit('stopLoading', 'maintenanceState');
        },
    },
    mutations: {
        setMaintenance(state, maintenanceState: MaintenanceState) {
            state.maintenance = maintenanceState;
        },
        setOffscreenMenuShow(state, value: boolean) {
            state.offscreenMenuShow = value;
        },
        updateNow(state) {
            state.now = Date.now();
        },
        startLoading(state, loadingType: string) {
            state.loading[loadingType] = true;
        },
        stopLoading(state, loadingType: string) {
            state.loading[loadingType] = false;
        },
        SOCKET_RECONNECT(state, event) {
            logger.log('Trying to reconnect websocket', event);
        },
        SOCKET_RECONNECT_ERROR(state, event) {
            logger.error('Could not reconnect websocket', event);
        },
        SOCKET_ONOPEN(state, event) {
            Vue.prototype.$socket = event.currentTarget;
            state.reconnecting = false;
        },
        SOCKET_ONCLOSE(state) {
            state.reconnecting = true;
        },
        SOCKET_ONERROR(state, event) {
            logger.error('Websocket error', event);
        },
        SOCKET_UpdateLoad(state, message) {
            state.clusterWorkload = message.load;
        },
        SOCKET_MaintenanceAlert(state, maintenanceState) {
            // notification handled in App.vue
            logger.log('Maintenance alert', maintenanceState);
            state.maintenance = maintenanceState;
        },
        SOCKET_ONMESSAGE(state, message) {
            logger.log('Uncaught message from websocket', message);
        },
        SOCKET_Login() {
            logger.log('Logged in by websocket');
            // handled in App.vue
        },
        SOCKET_Logout() {
            logger.log('Logged out by websocket');
            // handled in App.vue
        },
        SOCKET_ShowNotification() {
            // handled in App.vue
        },
        SOCKET_ShowJobNotification() {
            // handled in App.vue
        },
        SOCKET_WatchLogFile() {
            // handled in JobRunningTab.vue
        },
    },
    modules: {
        tools,
        jobs,
        auth,
    },
};

if (process.env.NODE_ENV !== 'development') {
    store.plugins = [localStoragePlugin];
}

export default new Vuex.Store<RootState>(store);
