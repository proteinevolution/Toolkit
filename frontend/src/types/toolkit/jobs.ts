import {JobState} from '@/types/toolkit/enums';

export interface Job {
    jobID: string;
    status: JobState;
    tool: string;
    code: string;
    parentID?: string;
    watched: boolean;
    foreign: boolean;
    isPublic: boolean;
    dateCreated?: number;
    dateUpdated?: number;
    dateViewed?: number;
    paramValues?: { string: any };
    views?: string[];
    alignments?: AlignmentItem[];
}

export interface SubmissionResponse {
    successful: boolean;
    code: number;
    message: string;
    jobID: string;
}

export interface CustomJobIdValidationResult {
    exists: boolean;
    suggested?: string;
}

export interface SimilarJobResult {
    jobID: string;
    dateCreated: number;
}

export interface AlignmentItem {
    accession: string;
    seq: string;
    num: number;
}
