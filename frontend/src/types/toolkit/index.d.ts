export interface Tool {
    name: string;
    longname: string;
    title?: string;
    code?: string;
    section: string;
    forwarding: ForwardingMode;

    parameters?: Parameter[];
}

export interface ForwardingMode {
    alignment: string[];
    multi_seq: string[];
}

export interface Parameter {
    type: string;
    name: string;
    label: string;
    section: string;
}

export interface TextAreaParameter extends Parameter {
    allowsTwoTextAreas: boolean;
    input_placeholder: string;
}

export interface SelectParameter extends Parameter {
    options: string[];
}
