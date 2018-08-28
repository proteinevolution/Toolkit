<template>
    <b-container>
        <b-row><h3>{{ tool.longname }}</h3></b-row>
        <b-row>
            <b-tabs>
                <b-tab v-for="section in parameterSections" :title="section.name">
                    <b-container>
                        <b-row v-for="parameter in section.parameters">
                            <component :is="parameter.type" :parameter="parameter.parameters"></component>
                        </b-row>
                    </b-container>
                </b-tab>
            </b-tabs>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import TextArea from '@/components/parameters/TextArea.vue';
    import {Parameter, ParameterSection, Tool} from '../types/toolkit';

    export default Vue.extend({
        name: 'ToolView',
        components: {
            TextArea,
        },
        computed: {
            toolName(): string {
                return this.$route.params.toolName;
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].filter((tool: Tool) => tool.name === this.toolName)[0];
            },
            parameterSections(): ParameterSection[] | undefined {
                return this.tool.parameters;
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
