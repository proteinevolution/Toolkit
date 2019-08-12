import {GetterTree} from 'vuex';
import {RootState, ToolState} from '../../types';
import {Tool} from '@/types/toolkit/tools';

const getters: GetterTree<ToolState, RootState> = {
    tools(state): Tool[] {
        return state.tools;
    },
};

export default getters;
