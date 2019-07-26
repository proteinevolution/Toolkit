export interface HHompHitsResponse {
    hits: HHompHitItem[] | any[];
    total: number;
    totalNoFilter: number;
    start: number;
    end: number;
}

export interface HHompAlignmentsResponse {
    alignments: HHompAlignmentItem[] | any[];
    total: number;
    start: number;
    end: number;
}

export interface HHompHitItem {
    num: number;
    acc: string;
    name: string;
    alignedCols: number;
    probabHit: number;
    probabOMP: number;
    eval: number;
    ssScore: number;
    templateRef: number;
}

export interface HHompAlignmentItem {
    num: number;
    acc: string;
    name: string;
    alignedCols: number;
    probabHit: number;
    probabOMP: number;
    eval: number;
    score: number;
    identities: number;
    // TODO type
    query: any;
    template: any;
}

export interface AlignmentResultResponse {
    alignments: AlignmentItem[];
    total: number;
    start: number;
    end: number;
}

export interface AlignmentItem {
    num: number;
    accession: string;
    seq: string;
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

export interface ProbEvalList {
    type: string;
    vals: number[];
}

export interface HitMapResponse {
    hitAreas: HitMapItem[];
    queryLength: number;
    resubmitStart: number;
    resubmitEnd: number;
}

export interface HitMapItem {
    num: number;
    title: string;
    b: number;
    t: number;
    l: number;
    r: number;
}