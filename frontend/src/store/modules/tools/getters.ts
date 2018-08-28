import {GetterTree} from 'vuex';
import {RootState, ToolState} from '../../types';
import {Parameter, Tool} from '@/types/toolkit';

const getters: GetterTree<ToolState, RootState> = {
    tools(state): Tool[] {
        return state.tools;
    },
    sections(state): string[] {
        return [...new Set(state.tools.map((tool: Tool) => tool.section))];
    },
};

export default getters;
