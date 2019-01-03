import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import jobs from './modules/jobs';
import {RootState} from './types';
import localStoragePlugin from './plugins/localStoragePlugin';
import {devMode} from '@/main';

Vue.use(Vuex);

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
        SOCKET_ONOPEN(state, event) {
            Vue.prototype.$socket = event.currentTarget;
            state.reconnecting = false;
        },
        SOCKET_ONCLOSE(state, event) {
            state.reconnecting = true;
        },
        SOCKET_ONERROR(state, event) {
            console.error(state, event);
        },
        UpdateLoad(state, message) {
            state.clusterWorkload = message.load;
        },
        SOCKET_ONMESSAGE(state, message) {
            console.log(message);
        },
    },
    modules: {
        tools,
        jobs,
    },
};

if (!devMode) {
    store.plugins = [localStoragePlugin];
}

export default new Vuex.Store<RootState>(store);
