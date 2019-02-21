import {MutationTree} from 'vuex';
import {AuthState} from '../../types';

const mutations: MutationTree<AuthState> = {
    setUser(state, user) {
        state.user = user;
    },
};

export default mutations;
