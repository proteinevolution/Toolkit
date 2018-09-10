// TODO: move content into different files
import {AlignmentSeqFormat, ParameterType, TextAreaInputType} from './enums';

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

export interface TextAreaParameter extends Parameter {
    inputType: TextAreaInputType;
    inputPlaceholder: string;
    allowsTwoTextAreas: boolean;
    alignmentValidation: AlignmentValidation;
}

export interface AlignmentValidation {
    allowedSeqFormats: AlignmentSeqFormat[];
    minCharPerSeq?: number;
    maxCharPerSeq?: number;
    minNumSeq?: number;
    maxNumSeq?: number;
    requiresSameLengthSeq?: boolean;
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
