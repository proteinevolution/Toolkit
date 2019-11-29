// TODO: move content into different files
import {AlignmentSeqFormat, AlignmentSeqType, ParameterType, TextAreaInputType} from './enums';

export interface Tool {
    name: string;
    longname: string;
    description: string;
    section: string;
    version: string;
    validationParams: ValidationParams;

    parameters?: ToolParameters;
}

export interface ToolParameters {
    sections: ParameterSection[];
    forwarding?: ForwardingMode;
    hideSubmitButtons?: boolean;
}

export interface ValidationParams {

}

export interface SequenceValidationParams extends ValidationParams {
    allowedSeqFormats: AlignmentSeqFormat[];
    allowedSeqType: AlignmentSeqType;
    minCharPerSeq?: number;
    maxCharPerSeq?: number;
    minNumSeq?: number;
    maxNumSeq?: number;
    requiresSameLengthSeq?: boolean;
    allowEmptySeq?: boolean;
}

export interface ForwardingMode {
    alignment: string[];
    multiSeq: string[];
    templateAlignment?: string[];
}

export interface ForwardingApiOptions {
    disableSequenceLengthSelect: boolean;
    selectedItems: number[];
}

export interface ForwardingApiOptionsAlignment {
    selectedItems: number[];
    resultField: string;
}

export interface ParameterSection {
    name: string;
    multiColumnLayout: boolean;
    parameters: Parameter[];
}

export interface Parameter {
    parameterType: ParameterType;
    name: string;
}

export interface TextInputParameter extends Parameter {
    inputPlaceholder: string;
    regex?: string;
    sampleInput?: string;
    disableRemember?: boolean;
}

export interface TextAreaParameter extends Parameter {
    inputType: TextAreaInputType;
    placeholderKey: string;
    sampleInputKey: string;
    allowsTwoTextAreas: boolean;
}

export interface SelectParameter extends Parameter {
    options: SelectOption[];
    maxSelectedOptions: number;
    forceMulti?: boolean;
    default?: string;
    onDetectedMSA?: string;
}

export interface SelectOption {
    value: string;
    text: string;
    $isDisabled?: boolean;
}

export interface HHpredSelectsParameter extends Parameter {
    nameProteomes: string;
    options: SelectOption[];
    optionsProteomes: SelectOption[];
    maxSelectedOptions: number;
    default?: string;
    defaultProteomes?: string;
}

export interface NumberParameter extends Parameter {
    min?: number;
    max?: number;
    step?: number;
    default?: number;
}

export interface BooleanParameter extends Parameter {
    default: boolean;
}

export interface FrontendToolParameter extends Parameter {
    sampleInput: string;
    placeholderKey: string;
}

export interface MSAViewerSeq {
    name: string;
    id: string;
    seq: string;
}
