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
        <b-alert variant="danger"
                 :show="error"
                 v-text="error"/>
        <b-btn v-text="$t('auth.signUp')"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'RegisterForm',
        data() {
            return {
                username: '',
                email: '',
                password: '',
                passwordRepeat: '',
                privacyAccepted: false,
                error: null,
            };
        },
        methods: {
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
