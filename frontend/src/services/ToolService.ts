import {Parameter, TextAreaParameter, Tool, ParameterSection} from '@/types/toolkit';

// Temporary mock, replace with API calls later

export default class ToolService {

    public static fetchTools(): Promise<Tool[]> {
        return new Promise<Tool[]>((resolve, reject) => {
            setTimeout(() => {
                resolve(this.tools);
            }, 0);
        });
    }

    public static fetchToolParameters(toolName: string): Promise<ParameterSection[]> {
        return new Promise<ParameterSection[]>((resolve, reject) => {
            setTimeout(() => {
                resolve(this.parameters
                    .filter((tuple: [string, ParameterSection[]]) => tuple[0] === toolName)[0][1]);
            }, 0);
        });
    }

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
        {
            name: 'alignmenttool1',
            longname: 'Alignment Tool 1',
            title: 'Great tool 1',
            section: 'Alignment',
            forwarding: {
                alignment: [],
                multi_seq: [],
            },
            parameters: undefined,
        },
    ];

    private static parameters: Array<[string, ParameterSection[]]> = [
        ['searchtool1',
            [
                {
                    name: 'Input',
                    parameters: [
                        ({
                            type: 'TextArea',
                            name: 'alignment',
                            label: '',
                            section: 'input',
                            allowsTwoTextAreas: true,
                            input_placeholder: 'Enter a protein sequence/multiple sequence alignment in ' +
                                'FASTA/CLUSTAL/A3M format',
                        } as TextAreaParameter),
                    ],
                },
                {
                    name: 'Parameters',
                    parameters: [

                    ],
                },
            ],
        ],
    ];

}
