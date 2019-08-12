import axios from 'axios';
import {Tool, ToolParameters} from '@/types/toolkit/tools';

class ToolService {

    public fetchToolsVersion(): Promise<string> {
        return new Promise<string>(((resolve, reject) => {
            axios.get('/api/tools/version/')
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public fetchTools(): Promise<Tool[]> {
        return new Promise<Tool[]>((resolve, reject) => {
            axios.get('/api/tools/')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchToolParameters(toolName: string): Promise<ToolParameters> {
        return new Promise<ToolParameters>((resolve, reject) => {
            axios.get(`/api/tools/${toolName}/`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }
}

export const toolService = new ToolService();
