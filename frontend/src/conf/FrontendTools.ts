import {FrontendToolParameter, Tool} from '@/types/toolkit';
import {ParameterType} from '@/types/toolkit/enums';
import {inputClustal, singleProtSeq} from '@/services/sampleseq';

export const Reformat: Tool = {
    name: 'reformat',
    longname: 'Reformat',
    description: 'Sequence reformatting utility',
    section: 'utils',
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
                sampleInput: inputClustal,
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
                sampleInput: singleProtSeq,
                label: '',
                name: '',
            } as FrontendToolParameter)],
        }],
    },
};
