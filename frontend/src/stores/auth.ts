import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { User } from '@/types/toolkit/auth';
import { authService } from '@/services/AuthService';

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null);

    const loggedIn = computed(() => user.value !== null);

    async function fetchUserData() {
        user.value = await authService.fetchUserData();
    }

    return { user, loggedIn, fetchUserData };
});
