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
};

export default getters;
