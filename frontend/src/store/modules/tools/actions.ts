import {ActionTree} from 'vuex';
import ToolService from '@/services/ToolService';
import {RootState, ToolState} from '../../types';

const actions: ActionTree<ToolState, RootState> = {
    async fetchAllTools(context) {
        const tools = await ToolService.fetchAll();
        context.commit('setTools', tools);
    },
};

export default actions;
