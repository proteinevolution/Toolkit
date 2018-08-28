import {GetterTree} from 'vuex';
import {RootState, ToolState} from '../../types';
import {Parameter, Tool} from '@/types/toolkit';

const getters: GetterTree<ToolState, RootState> = {
    tools(state): Tool[] {
        return state.tools;
    },
    tool(state): (toolName: string) => Tool {
        return (toolName: string) => state.tools.filter((tool: Tool) => tool.name === toolName)[0];
    },
    toolParameters(state): (toolName: string) => Parameter[] | undefined {
        return (toolName: string) => state.tools.filter((tool: Tool) => tool.name === toolName)[0].parameters;
    },
    sections(state): string[] {
        return [...new Set(state.tools.map((tool: Tool) => tool.section))];
    },
};

export default getters;
