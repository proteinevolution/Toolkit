<template>
    <b-form @submit.prevent="onSubmit">
        <b-form-group :label="$t('auth.firstName')"
                      :invalid-feedback="$t('constraints.lengthMax', { max: 25 })">
            <b-form-input v-model="firstName"
                          type="text"
                          :state="firstNameState"
                          :placeholder="$t('auth.firstNameEnter')"
                          @change="validateFirstName" />
        </b-form-group>
        <b-form-group :label="$t('auth.lastName')"
                      :invalid-feedback="$t('constraints.lengthMax', { max: 25 })">
            <b-form-input v-model="lastName"
                          type="text"
                          :state="lastNameState"
                          :placeholder="$t('auth.lastNameEnter')"
                          @change="validateLastName" />
        </b-form-group>
        <b-form-group :label="$t('auth.eMail')"
                      :invalid-feedback="$t('constraints.email')">
            <b-form-input v-model="eMail"
                          type="text"
                          :state="eMailState"
                          @change="validateEmail" />
        </b-form-group>
        <b-form-group :label="$t('auth.country')">
            <b-form-select v-model="country"
                           :options="countries">
                <template #first>
                    <option :value="''"
                            v-text="$t('auth.countrySelect')"></option>
                </template>
            </b-form-select>
        </b-form-group>


        <ExpandHeight>
            <b-alert v-if="needsConfirmation"
                     variant="primary"
                     :show="true">
                <b-form-group :label="$t('auth.reenterPassword')">
                    <b-form-input v-model="password"
                                  type="password" />
                </b-form-group>
                <b-btn class="mr-1"
                       @click="cancel"
                       v-text="$t('cancel')" />
                <b-btn :disabled="!valid"
                       type="submit"
                       variant="primary"
                       v-text="$t('submit')" />
            </b-alert>
        </ExpandHeight>

        <b-alert variant="danger"
                 :show="message !== ''"
                 v-text="message" />

        <b-btn :type="needsConfirmation ? 'button' : 'submit'"
               :disabled="!editButtonEnabled"
               @click="needsConfirmation = true"
               v-text="$t('auth.editProfile')" />
    </b-form>
</template>

<script lang="ts">
import Vue from 'vue';
import {AuthMessage, ProfileData, User} from '@/types/toolkit/auth';
import countries from '@/i18n/lang/countries';
import ExpandHeight from '@/transitions/ExpandHeight.vue';
import {authService} from '@/services/AuthService';
import {TranslateResult} from 'vue-i18n';
import {mapStores} from 'pinia';
import {useAuthStore} from '@/stores/auth';

const options = countries.map((value: string[]) => ({value: value[0], text: value[1]}));

export default Vue.extend({
    name: 'Profile',
    components: {
        ExpandHeight,
    },
    data() {
        return {
            needsConfirmation: false,
            firstName: '',
            firstNameState: null as boolean | null,
            lastName: '',
            lastNameState: null as boolean | null,
            eMail: '',
            eMailState: null as boolean | null,
            country: '',
            countries: options,
            password: '',
            message: '' as TranslateResult,
        };
    },
    computed: {
        user(): User | null {
            return this.authStore.user;
        },
        editButtonEnabled(): boolean {
            if (this.user) {
                return this.firstName !== this.user.nameFirst
                    || this.lastName !== this.user.nameLast
                    || this.eMail !== this.user.eMail
                    || this.country !== this.user.country;
            }
            return false;
        },
        firstNameValid(): boolean {
            return /^.{0,25}$/.test(this.firstName);
        },
        lastNameValid(): boolean {
            return /^.{0,25}$/.test(this.lastName);
        },
        eMailValid(): boolean {
            return /^\S+@\S+$/.test(this.eMail);
        },
        passwordValid(): boolean {
            return this.password !== '';
        },
        valid(): boolean {
            return this.firstNameValid
                && this.lastNameValid
                && this.eMailValid
                && this.passwordValid;
        },
        ...mapStores(useAuthStore),
    },
    watch: {
        user: {
            immediate: true,
            handler() {
                this.resetValues();
            },
        },
    },
    methods: {
        validateFirstName() {
            this.firstNameState = this.firstNameValid ? null : false;
        },
        validateLastName() {
            this.lastNameState = this.lastNameValid ? null : false;
        },
        validateEmail() {
            this.eMailState = this.eMailValid ? null : false;
        },
        resetValues() {
            if (this.user) {
                this.firstName = this.user.nameFirst;
                this.lastName = this.user.nameLast;
                this.eMail = this.user.eMail;
                this.country = this.user.country;
            }
        },
        cancel() {
            this.needsConfirmation = false;
            this.resetValues();
        },
        onSubmit() {
            if (this.needsConfirmation) {
                this.editProfileSubmit();
            } else {
                this.needsConfirmation = true;
            }
        },
        async editProfileSubmit() {
            if (!this.valid) {
                return;
            }
            const data: ProfileData = {
                nameLogin: this.user !== null ? this.user.nameLogin : '',
                nameFirst: this.firstName,
                nameLast: this.lastName,
                eMail: this.eMail,
                country: this.country,
                password: this.password,
            };
            try {
                const msg: AuthMessage = await authService.editProfile(data);
                if (msg.successful) {
                    if (msg.user !== null) {
                        this.authStore.user = msg.user;
                    }
                    this.$alert(this.$t('auth.responses.' + msg.messageKey, msg.messageArguments));
                } else {
                    this.message = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                }
            } catch (error) {
                this.message = this.$t('auth.responses.' + error.messageKey, error.messageArguments);
            }
            this.cancel();
        },
    },
});
</script>
