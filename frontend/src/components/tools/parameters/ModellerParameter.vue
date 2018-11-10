<template>
    <b-form-group :label="parameter.label">
        <b-form-input v-model="key"
                      type="text"
                      size="sm"
                      required>
        </b-form-input>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'ModellerParameter',
        data() {
            return {
                key: '',
            };
        },
    });
</script>

<style lang="scss" scoped>

</style>
