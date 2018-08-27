import {MutationTree} from 'vuex';
import {ToolState} from '../../types';
import {Tool} from '@/types/toolkit';

const mutations: MutationTree<ToolState> = {
    setTools(state, tools) {
        state.tools = tools;
    },
    setToolParameters(state, {toolName, parameters}) {
        state.tools.filter((tool: Tool) => tool.name === toolName)[0].parameters = parameters;
    },
};

export default mutations;
