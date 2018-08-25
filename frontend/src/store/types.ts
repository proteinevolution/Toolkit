import {Tool} from '@/types/toolkit';

export interface RootState {
    maintenanceMode: boolean;
    reconnecting: boolean;
}

export interface ToolState {
    tools: Tool[];
}
