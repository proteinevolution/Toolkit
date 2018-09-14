// Temporary mock, replace with API calls later


import {ParameterSection, Tool} from '../types/toolkit';
import {
    accessionIDParameter,
    alignmentModeParameter,
    booleanParameter,
    dnaSequenceParameter, fastaHeaderParameter,
    multiSelectParameter, pdbParameter,
    proteinSequenceParameter, reformatView,
    regexParameter,
    singleSelectParameter,
} from './exampleParams';

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
            showSubmitButtons: true,
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
            showSubmitButtons: true,
            parameters: undefined,
        },
        {
            name: 'patternsearchtool1',
            longname: 'PatternSearch',
            title: 'Great tool 1',
            section: 'Search',
            forwarding: {
                alignment: [],
                multiSeq: [],
            },
            showSubmitButtons: true,
            parameters: undefined,
        },
        {
            name: 'samcc',
            longname: 'SamCC',
            title: 'Great tool 1',
            section: 'ThreeAryStructure',
            forwarding: {
                alignment: [],
                multiSeq: [],
            },
            showSubmitButtons: true,
            parameters: undefined,
        },
        {
            name: 'retseq',
            longname: 'RetrieveSeq',
            title: 'Great tool 1',
            section: 'Utils',
            forwarding: {
                alignment: [],
                multiSeq: [],
            },
            showSubmitButtons: true,
            parameters: undefined,
        },
        {
            name: 'seq2id',
            longname: 'Seq2ID',
            title: 'Great tool 1',
            section: 'Utils',
            forwarding: {
                alignment: [],
                multiSeq: [],
            },
            showSubmitButtons: true,
            parameters: undefined,
        },
        {
            name: 'reformat',
            longname: 'Reformat',
            title: 'Great tool 1',
            section: 'Utils',
            forwarding: {
                alignment: [],
                multiSeq: [],
            },
            showSubmitButtons: false,
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
                        proteinSequenceParameter,
                    ],
                },
                {
                    name: 'Parameters',
                    multiColumnLayout: true,
                    parameters: [
                        singleSelectParameter,
                        multiSelectParameter,
                        alignmentModeParameter,
                        booleanParameter,
                    ],
                },
            ],
        ],
        ['alignmenttool1',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        dnaSequenceParameter,
                    ],
                },
            ],
        ],
        ['patternsearchtool1',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        regexParameter,
                    ],
                },
            ],
        ],
        ['samcc',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        pdbParameter,
                    ],
                },
            ],
        ],
        ['retseq',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        accessionIDParameter,
                    ],
                },
            ],
        ],
        ['seq2id',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        fastaHeaderParameter,
                    ],
                },
            ],
        ],
        ['reformat',
            [
                {
                    name: 'Input',
                    multiColumnLayout: false,
                    parameters: [
                        reformatView,
                    ],
                },
            ],
        ],
    ];

}
