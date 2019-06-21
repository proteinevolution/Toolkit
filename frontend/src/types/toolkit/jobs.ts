import {JobState} from '@/types/toolkit/enums';

export interface Job {
    jobID: string;
    status: JobState;
    tool: string;
    code: string;
    parentID?: string;
    watched?: boolean;
    foreign?: boolean;
    public?: boolean;
    dateCreated?: number;
    dateUpdated?: number;
    dateViewed?: number;
    paramValues?: object;
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
