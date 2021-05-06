import {GetterTree} from 'vuex';
import {AuthState, RootState} from '../../types';
import {User} from '@/types/toolkit/auth';

const getters: GetterTree<AuthState, RootState> = {
    user(state): User | null {
        return state.user;
    },
    loggedIn(state): boolean {
        return state.user !== null;
    },
    isAdmin(state): boolean {
        return state.user !== null && state.user.isAdmin;
    }
};

export default getters;
