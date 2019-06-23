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
    SEQUENCE = 'SEQUENCE',
    REGEX = 'REGEX',
    PDB = 'PDB',
    ACCESSION_ID = 'ACCESSION_ID',
}

export enum AlignmentSeqFormat {
    FASTA = 'FASTA',
    CLUSTAL = 'CLUSTAL',
    A3M = 'A3M',
    STOCKHOM = 'STOCKHOLM',
    PIR = 'PIR',
}

export enum AlignmentSeqType {
    PROTEIN = 'PROTEIN',
    DNA = 'DNA',
    RNA = 'RNA',
}

export enum JobState {
    Prepared = 1,
    Queued = 2,
    Running = 3,
    Error = 4,
    Done = 5,
    Submitted = 6,
    Pending = 7,
    LimitReached = 8,
}

export enum WebSocketActions {
    SET_JOB_WATCHED = 'SET_JOB_WATCHED',
}
