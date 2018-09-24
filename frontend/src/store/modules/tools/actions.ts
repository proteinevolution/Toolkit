import {ActionTree} from 'vuex';
import ToolService from '@/services/ToolService';
import {RootState, ToolState} from '../../types';
import {Tool} from '@/types/toolkit';

const actions: ActionTree<ToolState, RootState> = {
    async fetchAllTools(context) {
        const tools = await ToolService.fetchTools();
        context.commit('setTools', tools);
    },
    async fetchToolParametersIfNotPresent(context, toolName: string) {
        const tool: Tool = context.state.tools.filter((t: Tool) => t.name === toolName)[0];
        if (tool && !tool.parameters) {
            const parameters = await ToolService.fetchToolParameters(toolName);
            context.commit('setToolParameters', {toolName, parameters});
        }
    },
};

export default actions;
