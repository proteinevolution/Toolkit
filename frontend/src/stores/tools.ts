import { Tool, ToolParameters } from '@/types/toolkit/tools';
import { defineStore } from 'pinia';
import { toolService } from '@/services/ToolService';
import { AlignmentViewer, Reformat } from '@/conf/FrontendTools';
import { useStorage } from '@vueuse/core';
import { useRootStore } from '@/stores/root';

export const useToolsStore = defineStore('tools', () => {
    const version = useStorage('toolsVersion', '');
    const tools = useStorage<Tool[]>('tools', []);

    const rootStore = useRootStore();

    async function fetchAllTools() {
        rootStore.loading.tools = true;
        const newVersion = await toolService.fetchToolsVersion();
        if (newVersion !== version.value) {
            const newTools = await toolService.fetchTools();
            newTools.unshift(AlignmentViewer);
            newTools.push(Reformat);
            tools.value = newTools;
            version.value = newVersion;
        }
        rootStore.loading.tools = false;
    }

    async function fetchToolParametersIfNotPresent(toolName: string) {
        const tool: Tool = tools.value.filter((t: Tool) => t.name === toolName)[0];
        if (tool && !tool.parameters) {
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
            tools.value.filter((tool: Tool) => tool.name === toolName)[0].parameters = parameters;
            rootStore.loading.toolParameters = false;
        }
    }

    return { tools, version, fetchAllTools, fetchToolParametersIfNotPresent };
});
