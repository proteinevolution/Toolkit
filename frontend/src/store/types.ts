import {Tool} from '@/types/toolkit/tools';

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
