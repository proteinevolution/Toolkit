import axios from 'axios';
import {Tool, ToolParameters} from '@/types/toolkit/tools';

export default class ToolService {

    public static fetchToolsVersion(): Promise<string> {
        return new Promise<string>(((resolve, reject) => {
            axios.get('/api/tools/version/')
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public static fetchTools(): Promise<Tool[]> {
        return new Promise<Tool[]>((resolve, reject) => {
            axios.get('/api/tools/')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public static fetchToolParameters(toolName: string): Promise<ToolParameters> {
        return new Promise<ToolParameters>((resolve, reject) => {
            axios.get(`/api/tools/${toolName}/`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public static fetchToolHelp(toolName: string): Promise<string> {
        return new Promise<string>((resolve, reject) => {
            axios.get(`/api/tools/help/${toolName}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }
}
