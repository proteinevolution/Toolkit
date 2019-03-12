import axios from 'axios';
import {CustomJobIdValidationResult} from '@/types/toolkit/jobs';
import {AuthMessage, LoginData, SignUpData, User} from '@/types/toolkit/auth';

export default class AuthService {

    public static fetchUserData(): Promise<User> {
        return new Promise<User>(((resolve, reject) => {
            axios.get(`/api/auth/user/data`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }


    public static performLogin(data: LoginData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/login`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public static logout(): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.get(`/api/auth/logout`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public static signUp(data: SignUpData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/signup`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public static validateModellerKey(key: string): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/auth/validate/modeller?input=${key}`)
                .then((response) => resolve(response.data.isValid))
                .catch(reject);
        }));
    }

    public static validateJobId(jobId: string): Promise<CustomJobIdValidationResult> {
        return new Promise<CustomJobIdValidationResult>(((resolve, reject) => {
            axios.get(`/api/jobs/check/jobid/${jobId}/?resubmitJobID=null`) // TODO: what does resubmitJobID do?
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

}
