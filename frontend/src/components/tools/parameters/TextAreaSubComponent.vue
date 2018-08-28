<template>
    <b-form-group class="textarea-group">
        <b-form-textarea :id="id"
                         class="textarea-alignment"
                         :placeholder="input_placeholder"
                         v-model="text"
                         rows="14"
                         max-rows="14"
                         cols="70"
                         required
                         spellcheck="false">
        </b-form-textarea>
        <b-btn variant="link">Paste Example</b-btn>
        <b-btn variant="link">Upload File</b-btn>
    </b-form-group>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'TextAreaSubComponent',
        props: {
            id: String,
            input_placeholder: String,
        },
        data() {
            return {
                text: '',
            };
        },
    });
</script>

<style scoped>
    .textarea-group {
        width: 100%;
    }

    .textarea-alignment {
        font-family: Consolas, "SFMono-Regular", "Source Code Pro", "Liberation Mono", Menlo, Courier, monospace;
        width: 100%;
    }
</style>