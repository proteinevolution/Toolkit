// Temporary mock, replace with API calls later


import {
    BooleanParameter,
    NumberParameter,
    ParameterSection,
    ParameterType,
    SelectParameter,
    TextAreaInputType,
    TextAreaParameter,
    Tool,
} from '../types/toolkit';

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
                multiSeq: [],
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
                multiSeq: [],
            },
            parameters: undefined,
        },
    ];

    private static numberParameter1: NumberParameter = {
        type: ParameterType.Number,
        name: 'number_parameter',
        label: 'Some Number Parameter',
        min: 0,
        max: 100,
        default: 20,
    };

    private static selectParameter1: SelectParameter = {
        type: ParameterType.Select,
        name: 'msa_gen_method',
        label: 'MSA generation method',
        options: [
            {value: 'option1', text: 'Option 1'},
            {value: 'option2', text: 'Option 2'},
            {value: 'option3', text: 'Option 3'},
        ],
        maxSelectedOptions: 1,
    };

    private static parameters: Array<[string, ParameterSection[]]> = [
        ['searchtool1',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        ({
                            type: ParameterType.TextArea,
                            name: 'alignment',
                            label: '',
                            inputType: TextAreaInputType.Protein,
                            allowsTwoTextAreas: true,
                            inputPlaceholder: 'Enter a protein sequence/multiple sequence alignment in ' +
                                'FASTA/CLUSTAL/A3M format',
                        } as TextAreaParameter),
                    ],
                },
                {
                    name: 'Parameters',
                    multiColumnLayout: true,
                    parameters: [
                        ToolService.selectParameter1,
                        ToolService.numberParameter1,
                        ({
                            type: ParameterType.Boolean,
                            name: 'boolean_paramter',
                            label: 'Some Boolean Parameter',
                            default: false,
                        } as BooleanParameter),

                        ({
                            type: ParameterType.Select,
                            name: 'msa_gen_method',
                            label: 'MSA generation method',
                            options: [
                                {value: 'option1', text: 'Option 1'},
                                {value: 'option2', text: 'Option 2'},
                                {value: 'option3', text: 'Option 3'},
                            ],
                        } as SelectParameter),
                    ],
                },
            ],
        ],
    ];

}
