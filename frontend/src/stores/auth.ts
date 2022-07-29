import {defineStore} from 'pinia';
import {User} from '@/types/toolkit/auth';
import {authService} from '@/services/AuthService';

export interface AuthState {
    user: User | null;
    isAdmin: boolean;
}

export const useAuthStore = defineStore('auth', {
    state: (): AuthState => ({
        user: null,
        isAdmin: false,
    }),
    getters: {
        loggedIn(state): boolean {
            return state.user !== null;
        },
        isAdmin(state): boolean {
            return state.user !== null && state.user.isAdmin;
        }
    },
    actions: {
        async fetchUserData() {
            this.user = await authService.fetchUserData();
        },
        setUser(user: User | null) {
            if (user === null) {
                this.user = null;
            } else {
                // remove null values, they are inconvenient for further use
                user.nameFirst = user.nameFirst == null ? '' : user.nameFirst;
                user.nameLast = user.nameLast == null ? '' : user.nameLast;
                user.country = user.country == null ? '' : user.country;
                this.user = user;
            }
        },
    }
});
