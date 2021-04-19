export interface User {
    nameLogin: string;
    nameFirst: string;
    nameLast: string;
    eMail: string;
    country: string;
    isAdmin: boolean;
}

export interface LoginData {
    nameLogin: string;
    password: string;
}

export interface SignUpData {
    nameLogin: string;
    password: string;
    eMail: string;
    acceptToS: boolean;
}

export interface ProfileData {
    nameLogin: string;
    nameFirst: string;
    nameLast: string;
    eMail: string;
    country: string;
    password: string;
}

export interface PasswordChangeData {
    passwordOld: string;
    passwordNew: string;
}

export interface PasswordResetData {
    passwordNew: string;
    token: string;
    nameLogin: string;
}

export interface ForgotPasswordData {
    eMailOrUsername: string;
}

export interface AuthMessage {
    messageKey: string;
    messageArguments: string[];
    successful: boolean;
    user: User | null;
}

export interface MaintenanceState {
    message: string;
    submitBlocked: boolean;
}
