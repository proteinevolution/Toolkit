import {Tool, Parameter, TextAreaParameter} from '@/types/toolkit';

// Temporary mock, replace with API calls later

export default class ToolService {

    private static tools: Tool[] = [
        {
            name: 'searchtool1',
            longname: 'Search Tool 1',
            title: 'Great tool 1',
            section: 'Search',
            forwarding: {
                alignment: [],
                multi_seq: [],
            },
            parameters: undefined,
        },
    ];

    private static parameters: [string, Parameter[]][] = [
        ['searchtool1',
            [
                ({
                    type: 'alignment',
                    name: 'alignment',
                    label: '',
                    section: 'input',
                    allowsTwoTextAreas: true,
                    input_placeholder: ';asdlfkjds',
                } as TextAreaParameter),
            ],
        ],
    ];

    public static fetchTools(): Promise<Tool[]> {
        return new Promise<Tool[]>((resolve, reject) => {
            setTimeout(() => {
                resolve(this.tools);
            }, 0);
        });
    }

    public static fetchToolParameters(toolName: string): Promise<Parameter[]> {
        return new Promise<Parameter[]>((resolve, reject) => {
            setTimeout(() => {
                resolve(this.parameters.filter((tuple: [string, Parameter[]]) => tuple[0] === toolName)[0][1]);
            }, 0);
        });
    }
}
