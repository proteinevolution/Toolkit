export interface Tool {
    name: string;
    longname: string;
    title?: string;
    code?: string;
    section: string;
    forwarding: ForwardingMode;

    parameters?: ParameterSection[];
}

export interface ForwardingMode {
    alignment: string[];
    multiSeq: string[];
}

export interface ParameterSection {
    name: string;
    multiColumnLayout: boolean;
    parameters: Parameter[];
}

export interface Parameter {
    type: ParameterType;
    name: string;
    label: string;
}

export enum ParameterType {
    TextArea = 'TextArea',
    Select = 'Select',
    Number = 'Number',
    Boolean = 'Boolean',
    ModellerKey = 'ModellerKey',
}

export enum TextAreaInputType {
    Protein = 'protein',
    DNA = 'dna',
    Regex = 'regex',
    PBD = 'pbd',
    PIR = 'pir',
}

export enum AlignmentSeqFormat {
    FASTA = 'FASTA',
    CLUSTAL = 'CLUSTAL',
    A3M = 'A3M',
}

export interface TextAreaParameter extends Parameter {
    inputType: TextAreaInputType;
    inputPlaceholder: string;
    allowsTwoTextAreas: boolean;
    alignmentValidation?: AlignmentValidation;
}

export interface AlignmentValidation {
    allowedSeqFormats: AlignmentSeqFormat[];
    minCharPerSeq: number;
    maxCharPerSeq: number;
    minNumSeq: number;
    maxNumSeq: number;
    requiresSameLengthSeq: boolean;
}

export interface SelectParameter extends Parameter {
    options: SelectOption[];
    maxSelectedOptions: number;
}

export interface SelectOption {
    value: string;
    text: string;
}

export interface NumberParameter extends Parameter {
    min: number;
    max: number;
    default: number;
}

export interface BooleanParameter extends Parameter {
    default: boolean;
}

/*
export interface ModellerKeyParameter extends Parameter {
    TODO
}
*/
