<template>
    <div>
        <b-form-group :label="$t('auth.username')">
            <b-form-input v-model="username"
                          type="text"
                          required>
            </b-form-input>
        </b-form-group>
        <b-form-group :label="$t('auth.password')">
            <b-form-input v-model="password"
                          type="password"
                          required>
            </b-form-input>
        </b-form-group>
        <b-alert variant="danger"
                 :show="message !== ''"
                 v-text="message"/>
        <b-btn @click="login"
               v-text="$t('auth.signIn')"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {LoginData} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';

    export default Vue.extend({
        name: 'LoginForm',
        data() {
            return {
                username: '',
                password: '',
                message: '',
            };
        },
        methods: {
            async login() {
                const data: LoginData = {
                    nameLogin: this.username,
                    password: this.password,
                };
                try {
                    const msg = await AuthService.performLogin(data);
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', msg.user);
                    }
                    this.message = msg.message;
                } catch (error) {
                    this.message = '';
                    this.$alert(error.message, 'danger');
                }
            },

        },
    });
</script>

<style lang="scss" scoped>

</style>
