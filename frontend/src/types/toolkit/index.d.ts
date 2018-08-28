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
    multi_seq: string[];
}

export interface ParameterSection {
    name: string;
    multiColumnLayout: boolean;
    parameters: Parameter[];
}

export interface Parameter {
    type: string;
    name: string;
    label: string;
}

export interface TextAreaParameter extends Parameter {
    allowsTwoTextAreas: boolean;
    input_placeholder: string;
}

export interface SelectParameter extends Parameter {
    options: SelectOption[];
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

export interface BooleanParamter extends Parameter {
    default: boolean;
}