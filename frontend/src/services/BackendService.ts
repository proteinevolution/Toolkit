import axios from 'axios';

class BackendService {

    public fetchMaintenanceMode(): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/backend/maintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }
}

export const backendService = new BackendService();
