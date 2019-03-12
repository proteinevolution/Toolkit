import {MutationTree} from 'vuex';
import {AuthState} from '../../types';

const mutations: MutationTree<AuthState> = {
    setUser(state, user) {
        // remove null values, they are inconvenient for further use
        user.nameFirst = user.nameFirst == null ? '' : user.nameFirst;
        user.nameLast = user.nameLast == null ? '' : user.nameLast;
        user.country = user.country == null ? '' : user.country;
        state.user = user;
    },
};

export default mutations;
