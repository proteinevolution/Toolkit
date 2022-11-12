import axios from 'axios';
import { MaintenanceState } from '@/types/toolkit/auth';
import { Statistics } from '@/types/toolkit/admin';

class BackendService {
    public async setMaintenanceState(state: MaintenanceState): Promise<void> {
        await axios.post(`/api/backend/maintenance`, state);
    }

    public async fetchMaintenanceState(): Promise<MaintenanceState> {
        const res = await axios.get<MaintenanceState>(`/api/backend/maintenance`);
        return res.data;
    }

    public fetchStatistics(fromDate: string, toDate: string): Promise<Statistics> {
        return new Promise<Statistics>((resolve, reject) => {
            const params = { fromDate, toDate };
            axios
                .get(`/api/backend/statistics`, { params })
                .then((response) => resolve(response.data))
                .catch(reject);
        });
    }
}

export const backendService = new BackendService();
