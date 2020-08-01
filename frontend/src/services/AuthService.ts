import axios from 'axios';
import {CustomJobIdValidationResult} from '@/types/toolkit/jobs';
import {
    AuthMessage,
    ForgotPasswordData,
    LoginData,
    PasswordChangeData,
    PasswordResetData,
    ProfileData,
    SignUpData,
    User,
} from '@/types/toolkit/auth';

class AuthService {

    public fetchUserData(): Promise<User> {
        return new Promise<User>(((resolve, reject) => {
            axios.get(`/api/auth/user/data`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public login(data: LoginData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/login`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public logout(): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.get(`/api/auth/logout`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public signUp(data: SignUpData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/signup`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public editProfile(data: ProfileData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/profile`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public changePassword(data: PasswordChangeData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/password`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public forgotPassword(data: ForgotPasswordData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/reset/password`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public resetPassword(data: PasswordResetData): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.post(`/api/auth/reset/password/change`, data)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public verifyToken(nameLogin: string, token: string): Promise<AuthMessage> {
        return new Promise<AuthMessage>(((resolve, reject) => {
            axios.get(`/api/auth/verify/${nameLogin}/${token}`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

    public validateModellerKey(key: string): Promise<boolean> {
        return new Promise<boolean>(((resolve, reject) => {
            axios.get(`/api/auth/validate/modeller?input=${key}`)
                .then((response) => resolve(response.data.isValid))
                .catch(reject);
        }));
    }

    public validateJobId(newJobID: string): Promise<CustomJobIdValidationResult> {
        return new Promise<CustomJobIdValidationResult>(((resolve, reject) => {
            axios.get(`/api/jobs/check/job-id/${newJobID}/`)
                .then((response) => resolve(response.data))
                .catch(reject);
        }));
    }

}

export const authService = new AuthService();
