<template>
    <b-form-group class="textarea-group">
        <b-form-textarea class="textarea-alignment"
                         :placeholder="inputPlaceholder"
                         v-model="text"
                         :rows="shrink ? 8 : 14"
                         cols="70"
                         required
                         spellcheck="false">
        </b-form-textarea>
        <b-btn variant="link"
               size="sm"
               class="mt-1">
            Paste Example
        </b-btn>
        <b-btn variant="link"
               size="sm"
               class="mt-1">
            Upload File
        </b-btn>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        props: {
            id: String,
            inputPlaceholder: String,
            shrink: Boolean,
        },
        data() {
            return {
                text: '',
            };
        },
    });
</script>

<style lang="scss" scoped>
    .textarea-group {
        width: 100%;
    }

    .textarea-alignment {
        font-family: $font-family-monospace;
        width: 100%;
    }
</style>