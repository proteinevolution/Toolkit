import {Tool, ToolParameters} from '@/types/toolkit/tools';
import {defineStore} from 'pinia';
import {toolService} from '@/services/ToolService';
import {AlignmentViewer, Reformat} from '@/conf/FrontendTools';
import Vue from 'vue';
import {useStorage} from '@vueuse/core';
import {useRootStore} from '@/stores/root';

export interface ToolState {
    version: string;
    tools: Tool[];
}

export const useToolsStore = defineStore('tools', {
    state: () => ({
        version: useStorage('toolsVersion', ''),
        tools: useStorage<Tool[]>('tools', []),
    }),
    actions: {
        async fetchAllTools() {
            const rootStore = useRootStore();
            rootStore.loading.tools = true;
            const version = await toolService.fetchToolsVersion();
            if (version !== this.version) {
                const tools = await toolService.fetchTools();
                tools.unshift(AlignmentViewer);
                tools.push(Reformat);
                this.tools = tools;
                this.version = version;
            }
            rootStore.loading.tools = false;
        },
        async fetchToolParametersIfNotPresent(toolName: string) {
            const tool: Tool = this.tools.filter((t: Tool) => t.name === toolName)[0];
            if (tool && !tool.parameters) {
                const rootStore = useRootStore();
                rootStore.loading.toolParameters = true;
                let parameters: ToolParameters | undefined;
                switch (toolName) {
                    case 'alnviz':
                        parameters = AlignmentViewer.parameters;
                        break;
                    case 'reformat':
                        parameters = Reformat.parameters;
                        break;
                    default:
                        // Parameters for frontend tools are not fetched
                        parameters = await toolService.fetchToolParameters(toolName);
                }
                Vue.set(this.tools.filter((tool: Tool) => tool.name === toolName)[0], 'parameters', parameters);
                rootStore.loading.toolParameters = false;
            }
        },
    }
});
