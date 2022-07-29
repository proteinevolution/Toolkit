<template>
    <div>
        <b-form @submit.prevent="login">
            <b-form-group :label="$t('auth.eMailOrUsername')">
                <b-form-input v-model="username"
                              type="text"
                              autofocus
                              required />
            </b-form-group>
            <b-form-group :label="$t('auth.password')">
                <b-form-input v-model="password"
                              type="password"
                              required />
            </b-form-group>
            <b-alert variant="danger"
                     :show="message !== ''"
                     v-text="message" />
            <b-btn type="submit"
                   v-text="$t('auth.signIn')" />
            <a class="password-link"
               @click.stop="toggleForgotContainer">
                {{ $t('auth.forgotPassword') }}
            </a>
        </b-form>

        <ExpandHeight>
            <b-alert v-if="forgot.show"
                     :variant="!forgot.message || forgot.successful ? 'primary' : 'danger'"
                     :show="true"
                     class="mt-3 mb-0">
                <b-form v-show="!(forgot.message && forgot.successful)"
                        :class="[forgot.successful ? '' : 'mb-3']"
                        @submit.prevent="forgotPasswordSubmit">
                    <b-form-group :label="$t('auth.forgotPasswordInstructions')">
                        <b-form-input v-model="forgot.eMailOrUsername"
                                      :placeholder="$t('auth.eMailOrUsername')"
                                      type="text" />
                    </b-form-group>
                    <b-btn :disabled="eMailOrUsernameInvalid"
                           type="submit"
                           variant="primary"
                           v-text="$t('submit')" />
                </b-form>
                <div v-show="forgot.message"
                     v-text="forgot.message"></div>
            </b-alert>
        </ExpandHeight>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {AuthMessage, ForgotPasswordData, LoginData} from '@/types/toolkit/auth';
import {authService} from '@/services/AuthService';
import EventBus from '@/util/EventBus';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import {TranslateResult} from 'vue-i18n';
import {mapStores} from 'pinia';
import {useRootStore} from '@/stores/root';
import {useJobsStore} from '@/stores/jobs';
import {useAuthStore} from '@/stores/auth';

export default Vue.extend({
    name: 'LoginForm',
    components: {
        ExpandHeight,
    },
    data() {
        return {
            username: '',
            password: '',
            message: '' as TranslateResult,
            forgot: {
                show: false,
                eMailOrUsername: '',
                successful: true,
                message: '' as TranslateResult,
            },
        };
    },
    computed: {
        eMailOrUsernameInvalid(): boolean {
            return this.forgot.eMailOrUsername === '';
        },
      ...mapStores(useRootStore, useAuthStore, useJobsStore),
    },
    methods: {
        async login() {
            const data: LoginData = {
                nameLogin: this.username,
                password: this.password,
            };
            this.rootStore.loading.login = true;
            try {
                const msg: AuthMessage = await authService.login(data);
                const message: TranslateResult = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                if (msg.successful) {
                    this.authStore.user = msg.user;
                    EventBus.$emit('hide-modal', 'auth');
                    this.$alert(message);
                    // get jobs of user
                    await this.jobsStore.fetchAllJobs();
                }
                this.message = message;
            } catch (error) {
                this.message = '';
                this.$alert(this.$t('auth.responses.' + error.messageKey, error.messageArguments), 'danger');
            }
            this.rootStore.loading.login = false;
        },
        async forgotPasswordSubmit() {
            if (this.eMailOrUsernameInvalid) {
                return;
            }
            const data: ForgotPasswordData = {
                eMailOrUsername: this.forgot.eMailOrUsername,
            };
            try {
                const msg: AuthMessage = await authService.forgotPassword(data);
                this.forgot.message = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                this.forgot.successful = msg.successful;
            } catch (error) {
                this.forgot.message = this.$t('auth.responses.' + error.messageKey, error.messageArguments);
                this.forgot.successful = false;
            }
        },
        toggleForgotContainer(): void {
            this.forgot.show = !this.forgot.show;
            if (!this.forgot.successful) {
                this.forgot.successful = true;
                this.forgot.message = '';
            }
        },
    },
});
</script>

<style lang="scss" scoped>
.password-link {
  padding: 0.375rem 0;
  float: right;
  color: $primary !important;
  cursor: pointer;
}
</style>
