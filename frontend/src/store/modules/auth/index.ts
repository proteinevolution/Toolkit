import {Module} from 'vuex';
import getters from './getters';
import actions from './actions';
import mutations from './mutations';
import {AuthState, RootState} from '../../types';

export const state: AuthState = {
    user: null,
    isAdmin: false,
};

const namespaced: boolean = true;

const auth: Module<AuthState, RootState> = {
    namespaced,
    state,
    getters,
    actions,
    mutations,
};

export default auth;
