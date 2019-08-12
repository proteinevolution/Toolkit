import {FrontendToolParameter, Tool} from '@/types/toolkit/tools';
import {ParameterType} from '@/types/toolkit/enums';

export const Reformat: Tool = {
    name: 'reformat',
    longname: 'Reformat',
    description: 'Sequence reformatting utility',
    section: 'utils',
    version: '',
    validationParams: {},
    parameters: {
        hideSubmitButtons: true,
        forwarding: {
            alignment: [],
            multiSeq: [],
        },
        sections: [{
            name: 'Input',
            multiColumnLayout: false,
            parameters: [({
                parameterType: ParameterType.ReformatView,
                sampleInput: 'inputClustal',
                placeholderKey: 'protMSA',
                label: '',
                name: '',
            } as FrontendToolParameter)],
        }],
    },
};

export const AlignmentViewer: Tool = {
    name: 'alnviz',
    longname: 'AlignmentViewer',
    description: 'BioJS multiple sequence alignment viewer',
    section: 'alignment',
    version: '',
    validationParams: {},
    parameters: {
        hideSubmitButtons: true,
        forwarding: {
            alignment: [],
            multiSeq: [],
        },
        sections: [{
            name: 'Input',
            multiColumnLayout: false,
            parameters: [({
                parameterType: ParameterType.AlignmentViewerView,
                sampleInput: 'multiProtSeq',
                placeholderKey: 'protMSA',
                label: '',
                name: '',
            } as FrontendToolParameter)],
        }],
    },
};
