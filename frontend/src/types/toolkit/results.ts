export interface AlignmentItem {
    accession: string;
    seq: string;
    num: number;
}

export interface QueryItem {
    header: string;
    sequence: string;
}

export interface Quick2dResults {
    jobID: string;
    query: QueryItem;
    results: {[key: string]: string};
}
