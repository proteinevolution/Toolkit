
export enum ParameterType {
    TextArea = 'TextArea',
    Select = 'Select',
    Number = 'Number',
    Boolean = 'Boolean',
    AlignmentMode = 'AlignmentMode',
    ModellerKey = 'ModellerKey',
}

// TODO does it make sense to have these types ?
export enum TextAreaInputType {
    Sequence = 'sequence',
    Regex = 'regex',
}

export enum AlignmentSeqFormat {
    FASTA = 'FASTA',
    CLUSTAL = 'CLUSTAL',
    A3M = 'A3M',
    STOCKHOM = 'STOCKHOLM',
    // PBD = 'pbd',
    // PIR = 'pir',
}

export enum AlignmentSeqType {
    PROTEIN = 'PROTEIN',
    DNA = 'DNA',
    RNA = 'RNA',
}
