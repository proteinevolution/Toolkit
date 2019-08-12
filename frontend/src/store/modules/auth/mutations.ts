import {MutationTree} from 'vuex';
import {AuthState} from '../../types';
import Logger from 'js-logger';

const logger = Logger.get('AuthStore');

const mutations: MutationTree<AuthState> = {
    setUser(state, user) {
        if (user === null) {
            state.user = null;
        } else {
            // remove null values, they are inconvenient for further use
            user.nameFirst = user.nameFirst == null ? '' : user.nameFirst;
            user.nameLast = user.nameLast == null ? '' : user.nameLast;
            user.country = user.country == null ? '' : user.country;
            state.user = user;
        }
    },
};

export default mutations;
