import Vue from 'vue';
import Vuex, {StoreOptions} from 'vuex';
import tools from './modules/tools';
import {RootState} from './types';

Vue.use(Vuex);

const store: StoreOptions<RootState> = {
    state: {
        maintenanceMode: false,
        reconnecting: false,
    },
    modules: {
        tools,
    },
};

export default new Vuex.Store<RootState>(store);
