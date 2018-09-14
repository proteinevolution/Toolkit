// TODO: move content into different files
import {AlignmentSeqFormat, AlignmentSeqType, ParameterType, TextAreaInputType} from './enums';

export interface Tool {
    name: string;
    longname: string;
    title?: string;
    code?: string;
    section: string;
    forwarding: ForwardingMode;
    showSubmitButtons: boolean;

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
    sampleInput: string;
    allowsTwoTextAreas: boolean;
    validationParams: ValidationParams;
}

export interface ValidationParams {

}

export interface AlignmentValidationParams extends ValidationParams {
    allowedSeqFormats: AlignmentSeqFormat[];
    allowedSeqType: AlignmentSeqType;
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
    $isDisabled?: boolean;
}

export interface NumberParameter extends Parameter {
    min: number;
    max: number;
    default: number;
}

export interface BooleanParameter extends Parameter {
    default: boolean;
}

export interface ReformatViewParameter extends Parameter {
    sampleInput: string;
}
