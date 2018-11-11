import {AlignmentSeqFormat, AlignmentSeqType, ParameterType, TextAreaInputType} from '../types/toolkit/enums';
import {
    BooleanParameter,
    FrontendToolParameter,
    NumberParameter,
    Parameter,
    SelectParameter,
    SequenceValidationParams,
    TextAreaParameter,
} from '../types/toolkit';
import {inputClustal, multiProtSeq, patternProt, protHeaders, singleDNASeq, singleProtSeq} from './sampleseq';

export const numberParameter: NumberParameter = {
    parameterType: ParameterType.NumberParameter,
    name: 'number_parameter',
    label: 'Some Number Parameter',
    min: 0,
    max: 100,
    default: 20,
};

export const booleanParameter: BooleanParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'boolean_paramter',
    label: 'Some Boolean Parameter',
    default: false,
};

export const singleSelectParameter: SelectParameter = {
    parameterType: ParameterType.TextAreaParameter,
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
    parameterType: ParameterType.TextAreaParameter,
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
    parameterType: ParameterType.AlignmentMode,
    name: 'alignmentmode',
    label: '',
};

// ----------- Text Area Parameters -----------------------


export const proteinSequenceParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'alignment',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: true,
    inputPlaceholder: 'Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL/A3M format',
    sampleInput: singleProtSeq,
};

export const proteinSequenceValidationParams: SequenceValidationParams = {
    allowedSeqFormats: [AlignmentSeqFormat.FASTA, AlignmentSeqFormat.CLUSTAL],
    allowedSeqType: AlignmentSeqType.PROTEIN,
    minCharPerSeq: 5,
    minNumSeq: 1,
    maxNumSeq: 2,
    requiresSameLengthSeq: true,
};

export const dnaSequenceParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'alignment',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL/A3M format',
    sampleInput: singleDNASeq,
};

export const dnaSequenceValidationParams: SequenceValidationParams = {
    allowedSeqFormats: [AlignmentSeqFormat.FASTA],
    allowedSeqType: AlignmentSeqType.DNA,
};

export const regexParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'regex',
    label: '',
    inputType: TextAreaInputType.Regex,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a PROSITE grammar/regular expression.',
    sampleInput: patternProt,
};

export const pdbParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'pdb',
    label: '',
    inputType: TextAreaInputType.PDB,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter PDB coordinates of a four-helical bundle.',
    sampleInput: '<Sample PDB Input>', // TODO pdb sample input logic
};

export const accessionIDParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'accessionID',
    label: '',
    inputType: TextAreaInputType.AccessionID,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter a newline separated list of identifiers and choose the corresponding database.',
    sampleInput: protHeaders,
};


export const fastaHeaderParameter: TextAreaParameter = {
    parameterType: ParameterType.TextAreaParameter,
    name: 'accessionID',
    label: '',
    inputType: TextAreaInputType.Sequence,
    allowsTwoTextAreas: false,
    inputPlaceholder: 'Enter protein sequences (or their headers) in FASTA format.',
    sampleInput: multiProtSeq,
};

export const fastaHeaderValidationParams: SequenceValidationParams = {
    allowedSeqFormats: [AlignmentSeqFormat.FASTA],
    allowedSeqType: AlignmentSeqType.PROTEIN,
    allowEmptySeq: true,
};

// ----------------- Reformat View Parameter -----------------------

export const reformatView: FrontendToolParameter = {
    parameterType: ParameterType.ReformatView,
    name: '',
    label: '',
    sampleInput: inputClustal,
};


