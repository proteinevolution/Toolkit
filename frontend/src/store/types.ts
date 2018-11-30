import {Tool} from '@/types/toolkit/tools';
import {Job} from '@/types/toolkit/jobs';

export interface LoadingState {
    [key: string]: boolean;
}

export interface RootState {
    loading: LoadingState;
    maintenanceMode: boolean;
    reconnecting: boolean;
}

export interface ToolState {
    version: string;
    tools: Tool[];
}

export interface JobState {
    jobs: Job[];
}
