// Temporary mock, replace with API calls later


import {ParameterSection, Tool} from '../types/toolkit';
import {
    alignmentModeParameter,
    booleanParameter,
    dnaSequenceParameter,
    multiSelectParameter,
    proteinSequenceParameter,
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
        {
            name: 'patternsearchtool1',
            longname: 'PatternSearch',
            title: 'Great tool 1',
            section: 'Search',
            forwarding: {
                alignment: [],
                multiSeq: [],
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
    ];

}
