import {MutationTree} from 'vuex';
import {ToolState} from '../../types';

const mutations: MutationTree<ToolState> = {
    setTools(state, tools) {
        state.tools = tools;
    },
};

export default mutations;
