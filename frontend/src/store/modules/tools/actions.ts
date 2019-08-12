import {ActionTree} from 'vuex';
import {toolService} from '@/services/ToolService';
import {RootState, ToolState} from '../../types';
import {Tool, ToolParameters} from '@/types/toolkit/tools';
import {AlignmentViewer, Reformat} from '@/conf/FrontendTools';

const actions: ActionTree<ToolState, RootState> = {
    async fetchAllTools(context) {
        context.commit('startLoading', 'tools', {root: true});
        const version = await toolService.fetchToolsVersion();
        if (version !== context.state.version) {
            const tools = await toolService.fetchTools();
            tools.unshift(AlignmentViewer);
            tools.push(Reformat);
            context.commit('setTools', tools);
            context.commit('setVersion', version);
        }
        context.commit('stopLoading', 'tools', {root: true});
    },
    async fetchToolParametersIfNotPresent(context, toolName: string) {
        const tool: Tool = context.state.tools.filter((t: Tool) => t.name === toolName)[0];
        if (tool && !tool.parameters) {
            context.commit('startLoading', 'toolParameters', {root: true});
            const parameters: ToolParameters = await toolService.fetchToolParameters(toolName);
            context.commit('setToolParameters', {toolName, parameters});
            context.commit('stopLoading', 'toolParameters', {root: true});
        }
    },
};

export default actions;
