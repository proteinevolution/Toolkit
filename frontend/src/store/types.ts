import {Tool} from '@/types/toolkit';

export interface LoadingState {
    [key: string]: boolean;
}

export interface RootState {
    loading: LoadingState;
    maintenanceMode: boolean;
    reconnecting: boolean;
}

export interface ToolState {
    tools: Tool[];
}
