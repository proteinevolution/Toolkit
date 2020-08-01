import {Tool} from '@/types/toolkit/tools';
import {Job} from '@/types/toolkit/jobs';
import {User} from '@/types/toolkit/auth';

export interface LoadingState {
    [key: string]: boolean;
}

export interface RootState {
    loading: LoadingState;
    offscreenMenuShow: boolean;
    maintenanceMode: boolean;
    reconnecting: boolean;
    clusterWorkload: number;
    now: number;
}

export interface ToolState {
    version: string;
    tools: Tool[];
}

export interface JobState {
    jobs: Job[];
}

export interface AuthState {
    user: User | null;
}
