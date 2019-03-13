import {ActionTree} from 'vuex';
import {AuthState, RootState} from '../../types';
import AuthService from '@/services/AuthService';
import {User} from '@/types/toolkit/auth';

const actions: ActionTree<AuthState, RootState> = {
    async fetchUserData(context) {
        const user: User = await AuthService.fetchUserData();
        if (user) {
            context.commit('setUser', user);
        }
    },
};

export default actions;
