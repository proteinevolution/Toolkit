import {AlignmentSeqFormat, AlignmentSeqType, ParameterType, TextAreaInputType} from '../types/toolkit/enums';
import {
    BooleanParameter,
    NumberParameter,
    Parameter,
    ReformatViewParameter,
    SelectParameter,
    TextAreaParameter,
} from '../types/toolkit';
import {inputClustal, multiProtSeq, patternProt, protHeaders, singleDNASeq, singleProtSeq} from './sampleseq';

export const numberParameter: NumberParameter = {
    type: ParameterType.Number,
    name: 'number_parameter',
    label: 'Some Number Parameter',
    min: 0,
    max: 100,
    default: 20,
};

export const booleanParameter: BooleanParameter = {
    type: ParameterType.Boolean,
    name: 'boolean_paramter',
    label: 'Some Boolean Parameter',
    default: false,
};

export const singleSelectParameter: SelectParameter = {
    type: ParameterType.Select,
    name: 'msa_gen_method2',
    label: 'MSA generation method',
    options: [
        {value: 'option1', text: 'Option 1'},
        {value: 'option2', text: 'Option 2'},
        {value: 'option3', text: 'Option 3'},
    ],
    maxSelectedOptions: 1,
};

export const multiSelectParameter: SelectParameter = {
    type: ParameterType.Select,
    name: 'msa_gen_method',
    label: 'MSA generation method',
    options: [
        {value: 'option1', text: 'Option 1'},
        {value: 'option2', text: 'Option 2'},
        {value: 'option3', text: 'Option 3'},
    ],
    maxSelectedOptions: 2,
};

export const alignmentModeParameter: Parameter = {
    type: ParameterType.AlignmentMode,
    name: 'alignmentmode',
    label: '',
};

// ----------- Text Area Parameters -----------------------


export const proteinSequenceParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'alignment',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: true,
    inputPlaceholder: 'Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL/A3M format',
    sampleInput: singleProtSeq,
    validationParams: {
        allowedSeqFormats: [AlignmentSeqFormat.FASTA, AlignmentSeqFormat.CLUSTAL],
        allowedSeqType: AlignmentSeqType.PROTEIN,
        minCharPerSeq: 5,
        minNumSeq: 1,
        maxNumSeq: 2,
        requiresSameLengthSeq: true,
    },
};

export const dnaSequenceParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'alignment',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL/A3M format',
    sampleInput: singleDNASeq,
    validationParams: {
        allowedSeqFormats: [AlignmentSeqFormat.FASTA],
        allowedSeqType: AlignmentSeqType.DNA,
    },
};

export const regexParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'regex',
    label: '',
    inputType: TextAreaInputType.Regex,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a PROSITE grammar/regular expression.',
    sampleInput: patternProt,
    validationParams: {},
};

export const pdbParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'pdb',
    label: '',
    inputType: TextAreaInputType.PDB,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter PDB coordinates of a four-helical bundle.',
    sampleInput: '<Sample PDB Input>', // TODO pdb sample input logic
    validationParams: {},
};

export const accessionIDParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'accessionID',
    label: '',
    inputType: TextAreaInputType.AccessionID,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a newline separated list of identifiers and choose the corresponding database.',
    sampleInput: protHeaders,
    validationParams: {},
};


export const fastaHeaderParameter: TextAreaParameter = {
    type: ParameterType.TextArea,
    name: 'accessionID',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter protein sequences (or their headers) in FASTA format.',
    sampleInput: multiProtSeq,
    validationParams: {
        allowedSeqFormats:  [AlignmentSeqFormat.FASTA_HEADERS, AlignmentSeqFormat.FASTA],
        allowedSeqType: AlignmentSeqType.PROTEIN,
    },
};

// ----------------- Reformat View Parameter -----------------------

export const reformatView: ReformatViewParameter = {
    type: ParameterType.ReformatView,
    name: '',
    label: '',
    sampleInput: inputClustal,
};


