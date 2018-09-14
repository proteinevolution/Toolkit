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
                                      :style="{borderTopColor: sectionColor}">
                            <transition-group name="list-complete">
                                <b-nav-item v-for="tool in tools"
                                            class="list-complete-item"
                                            :key="tool.name"
                                            :to="'/tools/' + tool.name"
                                            v-if="tool.section === selectedSection">
                                    {{tool.longname}}
                                </b-nav-item>
                            </transition-group>
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
                userSelectedSection: '',
                sectionColors: [
                    '#D0BA89',
                    '#ffbb55',
                    '#669933',
                    '#79A4C4',
                    '#666699',
                    '#D6ABAB',
                    '#CC3333',
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
            defaultSelectedSection(): string {
                const matchingTools = this.tools.filter((tool: Tool) => tool.name === this.$route.params.toolName);
                if (matchingTools.length > 0) {
                    return matchingTools[0].section;
                } else {
                    return this.tools[0] ? this.tools[0].section : '';
                }
            },
            selectedSection(): string {
                return this.userSelectedSection ? this.userSelectedSection : this.defaultSelectedSection;
            },
        },
        methods: {
            selectSection(section: string): void {
                this.userSelectedSection = section;
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
                font-size: 0.8em;
                transition: background-color 0.2s;
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
                min-height: 44px;

                a {
                    color: $tk-gray;
                }
            }
        }
    }

    .list-complete-item {
        transition: opacity .5s, transform .5s;
        display: inline-block;

        &.list-complete-enter,
        &.list-complete-leave-to {
            opacity: 0;
            transform: translateY(-15px);
        }

        &.list-complete-leave-to {
            transition: opacity 0s;
        }

        &.list-complete-leave-active {
            position: absolute;
            transform: translateY(-20px);
        }
    }
</style>
