export interface User {
    nameLogin: string;
    nameFirst: string;
    nameLast: string;
    eMail: string;
    country: string;
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

export interface AuthMessage {
    message: string;
    successful: boolean;
    user: User;
}

