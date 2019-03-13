<template>
    <div>

        <b-form-group :label="$t('auth.firstName')">
            <b-form-input v-model="firstName"
                          type="text"
                          :placeholder="$t('auth.firstNameEnter')">
            </b-form-input>
        </b-form-group>
        <b-form-group :label="$t('auth.lastName')">
            <b-form-input v-model="lastName"
                          type="text"
                          :placeholder="$t('auth.lastNameEnter')">
            </b-form-input>
        </b-form-group>
        <b-form-group :label="$t('auth.eMail')">
            <b-form-input v-model="eMail"
                          type="text">
            </b-form-input>
        </b-form-group>
        <b-form-group :label="$t('auth.country')">
            <b-form-select v-model="country"
                           :options="countries">
                <template slot="first">
                    <option :value="''" v-text="$t('auth.countrySelect')"></option>
                </template>
            </b-form-select>
        </b-form-group>


        <ExpandHeight>
            <b-alert variant="primary" show v-if="state === 1">
                <b-form-group :label="$t('auth.reenterPassword')">
                    <b-form-input v-model="password"
                                  type="password">
                    </b-form-input>
                </b-form-group>
                <b-btn @click="cancel"
                       v-text="$t('cancel')"
                       class="mr-1"/>
                <b-btn @click="editProfileSubmit"
                       :disabled="passwordInvalid"
                       v-text="$t('submit')" variant="primary"/>
            </b-alert>
        </ExpandHeight>

        <b-btn @click="state = 1"
               v-text="$t('auth.editProfile')"
               :disabled="!editButtonEnabled"/>

    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {User} from '@/types/toolkit/auth';
    import countries from '@/i18n/lang/countries';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';

    const options = countries.map((value: string[]) => ({value: value[0], text: value[1]}));

    export default Vue.extend({
        name: 'Profile',
        components: {
            ExpandHeight,
        },
        data() {
            return {
                state: 0,
                firstName: '',
                lastName: '',
                eMail: '',
                country: '',
                countries: options,
                password: '',
            };
        },
        computed: {
            user(): User | null {
                return this.$store.getters['auth/user'];
            },
            editButtonEnabled(): boolean {
                if (this.user) {
                    const changed = this.firstName !== this.user.nameFirst
                        || this.lastName !== this.user.nameLast
                        || this.eMail !== this.user.eMail
                        || this.country !== this.user.country;
                    return changed;
                }
                return false;
            },
            passwordInvalid(): boolean {
                return this.password === '';
            },
        },
        watch: {
            user: {
                immediate: true,
                handler(value: User) {
                    this.resetValues();
                },
            },
        },
        methods: {
            resetValues() {
                if (this.user) {
                    this.firstName = this.user.nameFirst;
                    this.lastName = this.user.nameLast;
                    this.eMail = this.user.eMail;
                    this.country = this.user.country;
                }
            },
            cancel() {
                this.state = 0;
                this.resetValues();
            },
            async editProfileSubmit() {

            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
