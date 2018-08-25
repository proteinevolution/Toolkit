import {Tool} from '@/types/toolkit';

export interface RootState {
    loggedIn: boolean;
}

export interface ToolState {
    tools: Tool[];
}
