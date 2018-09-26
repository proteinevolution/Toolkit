import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import {LoadingState, RootState} from './types';

Vue.use(Vuex);

const store: StoreOptions<RootState> = {
    strict: process.env.NODE_ENV !== 'production',
    state: {
        loading: {
            tools: false,
            toolParameters: false,
        },
        maintenanceMode: false,
        reconnecting: false,
    },
    mutations: {
        startLoading(state, loadingType: string) {
            state.loading[loadingType] = true;
        },
        stopLoading(state, loadingType: string) {
            state.loading[loadingType] = false;
        },
    },
    modules: {
        tools,
    },
};

export default new Vuex.Store<RootState>(store);
