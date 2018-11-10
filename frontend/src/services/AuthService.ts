import axios from 'axios';

export default class AuthService {

    public static validateModellerKey(key: string): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/auth/validate/modeller?input=${key}`)
                .then((response) => resolve(response.data.isValid))
                .catch(reject);
        }));
    }

}
