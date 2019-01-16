import {JobState} from '@/types/toolkit/enums';

export interface Job {
    jobID: string;
    status: JobState;
    tool: string;
    code: string;
    hidden?: boolean;
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
    version?: number;
    suggested?: string;
}

export interface SimilarJobResult {
    jobID: string;
    dateCreated: number;
}
