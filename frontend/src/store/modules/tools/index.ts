import {Module} from 'vuex';
import getters from './getters';
import actions from './actions';
import mutations from './mutations';
import {RootState, ToolState} from '../../types';

export const state: ToolState = {
    version: '',
    tools: [],
};

const namespaced: boolean = true;

const tools: Module<ToolState, RootState> = {
    namespaced,
    state,
    getters,
    actions,
    mutations,
};

export default tools;
