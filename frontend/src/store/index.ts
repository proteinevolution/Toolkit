import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import jobs from './modules/jobs';
import {RootState} from './types';
import localStoragePlugin from './plugins/localStoragePlugin';

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
        SOCKET_RECONNECT(state, event) {
            // TODO
        },
        SOCKET_ONOPEN(state, event) {
            Vue.prototype.$socket = event.currentTarget;
            state.reconnecting = false;
        },
        SOCKET_ONCLOSE(state, event) {
            state.reconnecting = true;
        },
        SOCKET_ONERROR(state, event) {
            // console.error(state, event);
        },
        SOCKET_UpdateLoad(state, message) {
            state.clusterWorkload = message.load;
        },
        SOCKET_ONMESSAGE(state, message) {
            // console.log(message);
            // messages which haven't been caught will end up here
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
