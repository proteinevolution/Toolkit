import axios from 'axios';
import {Tool, ToolParameters} from '@/types/toolkit/tools';

class ToolService {
    public async fetchToolsVersion(): Promise<string> {
        const res = await axios.get<string>('/api/tools/version/');
        return res.data;
    }

    public async fetchTools(): Promise<Tool[]> {
        const res = await axios.get<Tool[]>('/api/tools/');
        return res.data;
    }

    public async fetchToolParameters(toolName: string): Promise<ToolParameters> {
        const res = await axios.get<ToolParameters>(`/api/tools/${toolName}/`);
        return res.data;
    }
}

export const toolService = new ToolService();
