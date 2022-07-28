import axios from 'axios';
import {MaintenanceState} from '@/types/toolkit/auth';

class BackendService {
    public async setMaintenanceState(state: MaintenanceState): Promise<void> {
        await axios.post(`/api/backend/maintenance`, state);
    }

    public async fetchMaintenanceState(): Promise<MaintenanceState> {
        const res = await axios.get<MaintenanceState>(`/api/backend/maintenance`);
        return res.data;
    }
}

export const backendService = new BackendService();
