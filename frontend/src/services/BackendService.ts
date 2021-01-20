import axios from 'axios';

class BackendService {

    public fetchMaintenanceMode(): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/backend/maintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public startMaintenance(): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.post(`/api/backend/startmaintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public endMaintenance(): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.post(`/api/backend/endmaintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }
}

export const backendService = new BackendService();
