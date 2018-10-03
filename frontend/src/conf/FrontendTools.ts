import {ReformatViewParameter, Tool} from '@/types/toolkit';
import {ParameterType} from '@/types/toolkit/enums';
import {inputClustal} from '@/services/sampleseq';

export const Reformat: Tool = {
    name: 'reformat',
    longname: 'Reformat',
    description: 'Sequence reformatting utility',
    section: 'utils',
    validationParams: {},
    parameters: {
        showSubmitButtons: false,
        forwarding: {
            alignment: [],
            multiSeq: [],
        },
        sections: [{
            name: 'Input',
            multiColumnLayout: false,
            parameters: [({
                type: ParameterType.ReformatView,
                sampleInput: inputClustal,
                label: '',
                name: '',
            } as ReformatViewParameter)],
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
        showSubmitButtons: false,
        forwarding: {
            alignment: [],
            multiSeq: [],
        },
        sections: [{
            name: 'Input',
            multiColumnLayout: false,
            parameters: [{
                type: ParameterType.TextArea, // TODO custom view?
                label: '',
                name: '',
            }],
        }],
    },
};
