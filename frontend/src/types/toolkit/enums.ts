
export enum ParameterType {
    TextInputParameter = 'TextInputParameter',
    TextAreaParameter = 'TextAreaParameter',
    SelectParameter = 'SelectParameter',
    NumberParameter = 'NumberParameter',
    BooleanParameter = 'BooleanParameter',
    AlignmentMode = 'AlignmentMode',
    ModellerKey = 'ModellerKey',
    ReformatView = 'ReformatView',
    AlignmentViewerView = 'AlignmentViewerView',
}

export enum TextAreaInputType {
    Sequence = 'sequence',
    Regex = 'regex',
    PDB = 'pdb', // samcc
    AccessionID = 'accessionID', // retrieve Seq
}

export enum AlignmentSeqFormat {
    FASTA = 'FASTA',
    CLUSTAL = 'CLUSTAL',
    A3M = 'A3M',
    STOCKHOM = 'STOCKHOLM',
    PIR = 'pir', // modeller
}

export enum AlignmentSeqType {
    PROTEIN = 'PROTEIN',
    DNA = 'DNA',
    RNA = 'RNA',
}
