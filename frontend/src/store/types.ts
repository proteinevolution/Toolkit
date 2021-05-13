import {Tool} from '@/types/toolkit/tools';
import {Job} from '@/types/toolkit/jobs';
import {MaintenanceState, User} from '@/types/toolkit/auth';

export interface LoadingState {
    [key: string]: boolean;
}

export interface RootState {
    loading: LoadingState;
    offscreenMenuShow: boolean;
    tourFinished: boolean;
    maintenance: MaintenanceState;
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
    isAdmin: boolean;
}
