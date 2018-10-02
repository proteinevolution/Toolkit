import axios from 'axios';
import {Tool, ToolParameters} from '../types/toolkit';

export default class ToolService {

    public static fetchToolsVersion(): Promise<string> {
        return new Promise<string>(((resolve, reject) => {
            axios.get('ui/tools/version')
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public static fetchTools(): Promise<Tool[]> {
        return new Promise<Tool[]>((resolve, reject) => {
            axios.get('ui/tools')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public static fetchToolParameters(toolName: string): Promise<ToolParameters> {
        return new Promise<ToolParameters>((resolve, reject) => {
            resolve({sections: [], showSubmitButtons: true}); // TODO: this is a mock
        });
    }
}
