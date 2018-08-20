<template>
    <div class="hello">
        <h1>{{ msg }}</h1>
        <b-button variant="primary">{{ $t("buttonTest") }}</b-button>
        <b-btn v-b-modal.modal1>Launch demo modal</b-btn>

        <!-- Modal Component -->
        <b-modal id="modal1" title="Bootstrap-Vue">
            <p class="my-4">Hello from modal!</p>
        </b-modal>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";

    export default Vue.extend({
        name: 'HelloWorld',
        props: {
            msg: String,
        },
    });
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
    h3 {
        margin: 40px 0 0;
    }

    ul {
        list-style-type: none;
        padding: 0;
    }

    li {
        display: inline-block;
        margin: 0 10px;
    }

    a {
        color: #42b983;
    }
</style>
