<template>
    <div class="navbar-container">
        <b-navbar toggleable="md"
                  type="light">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-row>
                    <b-col>
                        <b-navbar-nav class="upper-nav">
                            <b-nav-item v-for="section in sections"
                                        :key="section"
                                        :class="[section === selectedSection ? 'active' : '']"
                                        @click="selectSection(section)">
                                {{ $t('tools.sections.' + section) }}
                            </b-nav-item>
                        </b-navbar-nav>

                        <b-navbar-nav class="lower-nav"
                                      :style="'border-top-color: ' + sectionColor">
                            <b-nav-item v-for="tool in tools"
                                        :key="tool.name"
                                        :to="'/tools/' + tool.name"
                                        v-show="tool.section === selectedSection">
                                {{tool.longname}}
                            </b-nav-item>
                        </b-navbar-nav>
                    </b-col>
                </b-row>
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
                sectionColors: [
                    '#D0BA89',
                    '#FFCC66',
                ],
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            sections(): string[] {
                return this.$store.getters['tools/sections'];
            },
            sectionColor(): string {
                const index = this.sections.indexOf(this.selectedSection) % this.sections.length;
                return this.sectionColors[index];
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

<style lang="scss" scoped>
    .navbar-container .navbar {
        padding-left: 0;
        max-width: 750px;

        .navbar-nav {
            .nav-item a {
                border-radius: 5px;
                text-shadow: 0 1px 1px #fefefe;
                padding: 1rem 1rem;
                font-size: .75em;
            }

            .nav-item.active a, .nav-item:hover a, .nav-item .nav-link.active {
                background: rgba(220, 220, 220, 0.5);
            }

            &.upper-nav a {
                color: #888;
                font-weight: bold;
            }

            &.lower-nav {
                border-top: 2px solid #D0BA89;
                padding-top: 4px;
                a {
                    color: $tk-gray;
                }
            }
        }
    }
</style>
