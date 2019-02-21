<template>
    <div>
        <b-form-group :label="$t('auth.username')">
            <b-form-input v-model="username"
                          type="text"
                          required>
            </b-form-input>
        </b-form-group>
        <b-form-group :label="$t('auth.email')">
            <b-form-input v-model="email"
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
        <b-form-group :label="$t('auth.passwordRepeat')">
            <b-form-input v-model="passwordRepeat"
                          type="password"
                          required>
            </b-form-input>
        </b-form-group>
        <b-form-group>
            <b-form-checkbox v-model="privacyAccepted">
                <i18n path="auth.privacyAccept" tag="span">
                    <a class="privacy-link"
                       @click.stop="openPrivacyPolicy">
                        {{ $t('footerLinkModals.names.privacy') }}
                    </a>
                </i18n>
            </b-form-checkbox>
        </b-form-group>
        <b-alert :variant="successful ? 'info' : 'danger'"
                 :show="message !== ''"
                 v-text="message"/>
        <b-btn @click="signUp"
               v-text="$t('auth.signUp')"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import EventBus from '@/util/EventBus';
    import AuthService from '@/services/AuthService';
    import {SignUpData} from '@/types/toolkit/auth';

    export default Vue.extend({
        name: 'RegisterForm',
        data() {
            return {
                username: '',
                email: '',
                password: '',
                passwordRepeat: '',
                privacyAccepted: false,
                successful: false,
                message: '',
            };
        },
        methods: {
            async signUp() {
                const data: SignUpData = {
                    nameLogin: this.username,
                    password: this.password,
                    eMail: this.email,
                    acceptToS: this.privacyAccepted,
                };
                try {
                    const msg = await AuthService.signUp(data);
                    this.successful = msg.successful;
                    this.message = msg.message;
                } catch (error) {
                    this.successful = false;
                    this.message = '';
                    this.$alert(error.message, 'danger');
                }
            },
            openPrivacyPolicy() {
                EventBus.$emit('show-modal', {id: 'simple', props: {modal: 'privacy'}});
            },
        },
    });
</script>

<style lang="scss" scoped>
    .privacy-link {
        color: $primary !important;
        cursor: pointer;
    }
</style>
