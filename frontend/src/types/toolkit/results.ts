export interface ResultHitsResponse {
    hits: HHompHitItem[] | any[];
    total: number;
    totalNoFilter: number;
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

export interface HHPredResults {
    jobID: string;
    query: QueryItem[];
    querytemplate: QueryItem[];
    reduced: QueryItem[];
    results: {
        COILPRED: string;
        MSA_GEN: string;
        QA3M_COUNT: number;
        TMPRED: string;
        alignment: any; // TODO type this
        db: string;
        hits: HHPredHit[];
        info: any; // TODO type this
        proteomes: string;
    };
}

export interface HHPredHit {
    cols: number;
    eval: number;
    hit: string;
    no: number;
    prob: number;
    pval: number;
    query_begin: number;
    query_end: number;
    ref: number;
    score: number;
    ss: number;
    struc: string;
    template_begin: number;
    template_end: number;
}
