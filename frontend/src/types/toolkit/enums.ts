
export enum ParameterType {
    TextArea = 'TextArea',
    Select = 'Select',
    Number = 'Number',
    Boolean = 'Boolean',
    AlignmentMode = 'AlignmentMode',
    ModellerKey = 'ModellerKey',
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
    FASTA_HEADERS = 'FASTA_HEADER', // Seq2ID
}

export enum AlignmentSeqType {
    PROTEIN = 'PROTEIN',
    DNA = 'DNA',
    RNA = 'RNA',
}
