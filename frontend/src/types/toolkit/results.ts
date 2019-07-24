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
        regex: string;
    };
}

export interface PatsearchHit {
    name: string;
    seq: string;
    matches: PatsearchMatch[];
}

export interface PatsearchMatch {
    i: number; // i: start index
    n: number; // n: length of match
}

export interface TprpredResults {
    desc: string[];
    hits: string[];
}

export interface HhrepidResults {
    jobID: string;
    results: {
        reptypes: HhrepidReptypes[];
    };
}

export interface HhrepidReptypes {
    pval: string;
    reps: HhrepidReptype[];
    len: number;
    typ: string;
    num: number;
}

export interface HhrepidReptype {
    prob: string;
    pval: string;
    loc: string;
    seq: string;
}
