<template>
    <div>

        <b-form-group :label="$t('auth.changePassword')">
            <b-form-input v-model="oldPassword"
                          type="password"
                          class="mb-3 mt-2"
                          :placeholder="$t('auth.oldPassword')">
            </b-form-input>
            <b-form-input v-model="newPassword"
                          type="password" class="mb-3"
                          :placeholder="$t('auth.newPassword')">
            </b-form-input>
            <b-form-input v-model="confirmPassword"
                          type="password" class="mb-3"
                          :placeholder="$t('auth.confirmPassword')">
            </b-form-input>
            <b-btn v-text="$t('auth.changePassword')"></b-btn>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';


    export default Vue.extend({
        name: 'Settings',
        components: {
            ExpandHeight,
        },
        data() {
            return {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
            };
        },

    });
</script>

<style lang="scss" scoped>
</style>
