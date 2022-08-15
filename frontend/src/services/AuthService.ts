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

    public async fetchUserData(): Promise<User> {
        const res = await axios.get<User>(`/api/auth/user/data`);
        return res.data;
    }

    public async login(data: LoginData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/login`, data);
        return res.data;
    }

    public async logout(): Promise<AuthMessage> {
        const res = await axios.get<AuthMessage>(`/api/auth/logout`);
        return res.data;
    }

    public async signUp(data: SignUpData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/signup`, data);
        return res.data;
    }

    public async editProfile(data: ProfileData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/profile`, data);
        return res.data;
    }

    public async changePassword(data: PasswordChangeData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/password`, data);
        return res.data;
    }

    public async forgotPassword(data: ForgotPasswordData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/reset/password`, data);
        return res.data;
    }

    public async resetPassword(data: PasswordResetData): Promise<AuthMessage> {
        const res = await axios.post<AuthMessage>(`/api/auth/reset/password/change`, data);
        return res.data;
    }

    public async verifyToken(nameLogin: string, token: string): Promise<AuthMessage> {
        const res = await axios.get<AuthMessage>(`/api/auth/verify/${nameLogin}/${token}`);
        return res.data;
    }

    public async validateModellerKey(key: string): Promise<boolean> {
        const res = await axios.get<any>(`/api/auth/validate/modeller?input=${key}`);
        return res.data.isValid;
    }

    public async validateJobId(newJobID: string): Promise<CustomJobIdValidationResult> {
        const res = await axios.get<CustomJobIdValidationResult>(`/api/jobs/check/job-id/${newJobID}/`);
        return res.data;
    }
}

export const authService = new AuthService();
