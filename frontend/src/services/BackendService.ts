import axios from 'axios';
import {MaintenanceState} from '@/types/toolkit/auth';

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
}

export const backendService = new BackendService();
