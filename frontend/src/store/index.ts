import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import jobs from './modules/jobs';
import {RootState} from './types';
import localStoragePlugin from './plugins/localStoragePlugin';
import Logger from 'js-logger';

Vue.use(Vuex);
const logger = Logger.get('Store');

const store: StoreOptions<RootState> = {
    strict: process.env.NODE_ENV !== 'production',
    state: {
        loading: {
            tools: false,
            toolParameters: false,
        },
        maintenanceMode: false,
        reconnecting: true,
        clusterWorkload: 0,
    },
    mutations: {
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
        SOCKET_ONCLOSE(state, event) {
            state.reconnecting = true;
        },
        SOCKET_ONERROR(state, event) {
            logger.error('Websocket error', event);
        },
        SOCKET_UpdateLoad(state, message) {
            state.clusterWorkload = message.load;
        },
        SOCKET_ONMESSAGE(state, message) {
            logger.log('Uncaught message from websocket', message);
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
    },
};

if (process.env.NODE_ENV !== 'development') {
    store.plugins = [localStoragePlugin];
}

export default new Vuex.Store<RootState>(store);
