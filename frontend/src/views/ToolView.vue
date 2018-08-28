<template>
    <b-container>
        {{ tool }}
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Tool} from '../types/toolkit';

    export default Vue.extend({
        name: 'ToolView',
        computed: {
            toolName(): string {
                return this.$route.params.toolName;
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].filter((tool: Tool) => tool.name === this.toolName);
            },
        },
        created() {
            this.loadToolParameters();
        },
        beforeRouteEnter(to, from, next) {
            next((vm) => {
                if (!vm.$store.getters['tools/tools'].some((tool: Tool) => tool.name === to.params.toolName)) {
                    next({path: '/404'});
                } else {
                    next();
                }
            });
        },
        beforeRouteUpdate(to, from, next) {
            this.loadToolParameters();
            next();
        },
        methods: {
            loadToolParameters() {
                this.$store.dispatch('tools/fetchToolParametersIfNotPresent', this.toolName);
            },
        },
    });
</script>
