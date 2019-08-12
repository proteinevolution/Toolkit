import {MutationTree} from 'vuex';
import Vue from 'vue';
import {ToolState} from '../../types';
import {Tool} from '@/types/toolkit/tools';

const mutations: MutationTree<ToolState> = {
    setTools(state, tools) {
        state.tools = tools;
    },
    setVersion(state, version) {
        state.version = version;
    },
    setToolParameters(state, {toolName, parameters}) {
        Vue.set(state.tools.filter((tool: Tool) => tool.name === toolName)[0], 'parameters', parameters);
    },
};

export default mutations;
