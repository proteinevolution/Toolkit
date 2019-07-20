export interface AlignmentResultResponse {
    alignments: AlignmentItem[];
    total: number;
    start: number;
    end: number;
}

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
    results: { [key: string]: string };
}

export interface PatsearchResults {
    jobID: string;
    results: {
        hits: PatsearchHit[];
        len: number;
        regex: string;
    };
}

export interface PatsearchHit {
    name: string;
    pats: number[];
    matches: string;
    seq: string;
}

export interface TprpredResults {
    desc: string[];
    hits: string[];
}
