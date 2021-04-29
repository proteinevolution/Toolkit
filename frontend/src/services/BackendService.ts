import axios from 'axios';
import {MaintenanceState} from '@/types/toolkit/auth';
import {Statistics} from '@/types/toolkit/admin';

class BackendService {

    public setMaintenanceState(state: MaintenanceState): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.post(`/api/backend/maintenance`, state)
                .then(() => resolve())
                .catch(reject);
        }));
    }

    public fetchMaintenanceState(): Promise<MaintenanceState> {
        return new Promise<MaintenanceState>(((resolve, reject) => {
            axios.get(`/api/backend/maintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public fetchStatistics(fromDate: string, toDate: string): Promise<Statistics> {
        return new Promise<Statistics>(((resolve, reject) => {
            const params = {fromDate, toDate};
            axios.get(`/api/backend/statistics`, {params})
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }
}

export const backendService = new BackendService();
