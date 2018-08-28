<template>
    <div id="navbar">
        <b-navbar toggleable="md"
                  type="light">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="section in sections"
                                :key="section"
                                :class="[section === selectedSection ? 'active' : '']"
                                @click="selectSection(section)">
                        {{ $t('tools.sections.' + section) }}
                    </b-nav-item>
                </b-navbar-nav>
            </b-collapse>
        </b-navbar>

        <b-navbar toggleable="md"
                  type="light">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="tool in tools"
                                :key="tool.name"
                                :to="'/tools/' + tool.name"
                                v-show="tool.section === selectedSection">
                        {{tool.longname}}
                    </b-nav-item>
                </b-navbar-nav>
            </b-collapse>
        </b-navbar>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Tool} from '../../types/toolkit';

    export default Vue.extend({
        name: 'NavBar',
        data() {
            return {
                selectedSection: '',
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            sections(): string[] {
                return this.$store.getters['tools/sections'];
            },
        },
        watch: {
            'sections'() {
                this.updateSelection();
            },
            '$route.params.toolName'() {
                this.updateSelection();
            },
        },
        methods: {
            selectSection(section: string): void {
                this.selectedSection = section;
            },
            updateSelection(): void {
                const matchingTools = this.tools.filter((tool: Tool) => tool.name === this.$route.params.toolName);
                if (matchingTools.length > 0) {
                    this.selectedSection = matchingTools[0].section;
                } else if (!this.selectedSection) {
                    this.selectedSection = this.tools[0].section;
                }
            },
        },
    });
</script>

<style>
</style>
