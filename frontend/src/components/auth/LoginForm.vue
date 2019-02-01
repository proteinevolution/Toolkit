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
                 :show="error"
                 v-text="error"/>
        <b-btn v-text="$t('auth.signIn')"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'LoginForm',
        data() {
            return {
                username: '',
                password: '',
                error: null,
            };
        },
    });
</script>

<style lang="scss" scoped>

</style>
