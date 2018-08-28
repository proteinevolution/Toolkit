import {Parameter, TextAreaParameter, Tool, ParameterSection} from '@/types/toolkit';
import {BooleanParamter, NumberParameter, SelectParameter} from '../types/toolkit';

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
                    multiColumnLayout: false,
                    parameters: [
                        ({
                            type: 'TextArea',
                            name: 'alignment',
                            label: '',
                            allowsTwoTextAreas: true,
                            input_placeholder: 'Enter a protein sequence/multiple sequence alignment in ' +
                                'FASTA/CLUSTAL/A3M format',
                        } as TextAreaParameter),
                    ],
                },
                {
                    name: 'Parameters',
                    multiColumnLayout: true,
                    parameters: [
                        ({
                            type: 'Select',
                            name: 'msa_gen_method',
                            label: 'MSA generation method',
                            options: [
                                { value: 'option1', text: 'Option 1' },
                                { value: 'option2', text: 'Option 2' },
                                { value: 'option3', text: 'Option 3' },
                            ],
                        } as SelectParameter),

                        ({
                            type: 'Number',
                            name: 'number_parameter',
                            label: 'Some Number Parameter',
                            min: 0,
                            max: 100,
                            default: 20,
                        } as NumberParameter),

                        ({
                            type: 'Boolean',
                            name: 'boolean_paramter',
                            label: 'Some Boolean Parameter',
                            default: false,
                        } as BooleanParamter),

                        ({
                            type: 'Select',
                            name: 'msa_gen_method',
                            label: 'MSA generation method',
                            options: [
                                { value: 'option1', text: 'Option 1' },
                                { value: 'option2', text: 'Option 2' },
                                { value: 'option3', text: 'Option 3' },
                            ],
                        } as SelectParameter),
                    ],
                },
            ],
        ],
    ];

}
