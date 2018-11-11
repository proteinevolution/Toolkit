import {FrontendToolParameter, Tool} from '@/types/toolkit';
import {ParameterType} from '@/types/toolkit/enums';
import {inputClustal, multiProtSeq} from '@/util/SampleSeqs';

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
                inputPlaceholder: '',
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
                sampleInput: multiProtSeq,
                inputPlaceholder: 'Enter Sequences in FASTA or CLUSTAL format.',
                label: '',
                name: '',
            } as FrontendToolParameter)],
        }],
    },
};
