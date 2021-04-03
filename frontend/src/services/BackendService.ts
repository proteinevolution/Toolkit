import axios from 'axios';

class BackendService {

    public setMaintenanceMessage(message: string): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.post(`/api/backend/maintenance/message`, {message: message})
                .then(() => resolve())
                .catch(reject);
        }));
    }

    public fetchMaintenanceMessage(): Promise<string> {
        return new Promise<string>(((resolve, reject) => {
            axios.get(`/api/backend/maintenance/message`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public fetchMaintenanceMode(): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/backend/maintenance`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public startMaintenance(): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.post(`/api/backend/maintenance/start`)
                .then(() => resolve())
                .catch(reject);
        }));
    }

    public endMaintenance(): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.post(`/api/backend/maintenance/end`)
                .then(() => resolve())
                .catch(reject);
        }));
    }
}

export const backendService = new BackendService();
