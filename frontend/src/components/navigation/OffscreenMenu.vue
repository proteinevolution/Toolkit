<template>
    <div class="offscreen-menu-wrapper"
         id="offscreenMenu">
        <transition name="fade">
            <div class="offscreen-backdrop"
                 @click="close"
                 v-if="isOpen">
            </div>
        </transition>
        <transition name="slide">
            <div class="offscreen-menu"
                 v-if="isOpen">

                <transition mode="out-in">
                    <b-nav vertical
                           class="mt-2"
                           v-if="!selectedSection"
                           key="top-level">
                        <b-nav-item key="home"
                                    @click="close"
                                    to="/">
                            Home
                        </b-nav-item>
                        <b-nav-item v-for="(section, i) in sections"
                                    :key="section"
                                    class="section-link"
                                    @click="selectedSection = section">
                            {{ $t(`tools.sections.${section}.title`) }}

                            <i class="fa fa-angle-right"
                               :style="{color: sectionColors[i]}"></i>
                        </b-nav-item>
                    </b-nav>
                    <b-nav vertical
                           class="mt-2"
                           v-else
                           key="bottom-level">
                        <b-nav-item key="back"
                                    @click="selectedSection = ''">
                            <i class="fa fa-angle-left mr-2"></i>
                            {{ $t(`back`) }}
                        </b-nav-item>
                        <b-nav-item v-for="tool in displayedTools"
                                    :key="tool.name"
                                    :to="'/tools/' + tool.name"
                                    @click="close">
                            {{tool.longname}}
                        </b-nav-item>
                    </b-nav>
                </transition>

                <JobList @click="close"/>

                <span class="offscreen-menu-close"
                      @click="close">
                    &times;
                </span>
            </div>
        </transition>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import JobList from '@/components/sidebar/JobList.vue';
    import {sectionColors, sections} from '@/conf/ToolSections';
    import {Tool} from '@/types/toolkit/tools';

    export default Vue.extend({
        name: 'OffscreenMenu',
        components: {
            JobList,
        },
        data() {
            return {
                selectedSection: '',
                sectionColors,
                sections,
            };
        },
        computed: {
            isOpen(): boolean {
                return this.$store.state.offscreenMenuShow;
            },
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            displayedTools(): Tool[] {
                return this.tools.filter((tool: Tool) => tool.section === this.selectedSection);
            },
        },
        methods: {



            close(): void {
                this.$store.commit('setOffscreenMenuShow', false);
                this.selectedSection = '';
            },
        },
    });
</script>

<style lang="scss" scoped>
    .offscreen-menu-wrapper {

        .offscreen-menu {
            position: fixed;
            top: 0;
            left: 0;
            height: 100vh;
            width: 100vw;
            max-width: 340px;
            background: $tk-lighter-gray;
            z-index: 100;
            transition: left 1s ease-in-out;

            .offscreen-menu-close {
                position: absolute;
                right: 0rem;
                top: 0.5rem;
                font-size: 2rem;
                color: $tk-dark-gray;
                cursor: pointer;
                line-height: 0.4;

                padding: 0.8rem 0.7rem 0.77rem 0.67rem;

                //border: 1px solid #dadce0;
                //border-radius: 50%;
                background-color: $tk-lighter-gray;
            }

            .offscreen-menu-close:hover{
                background: $tk-light-gray;
            }

            .nav-link {
                color: $tk-darker-gray;
            }

            // change colors with hover over menu links
            .nav-link:hover {
                background: $tk-light-gray;
            }

            .section-link .nav-link {
                display: flex;
                justify-content: space-between;
                align-items: center;

                color: $tk-darker-gray;
                i {
                    font-size: 1.4em;
                }
            }
        }

        .offscreen-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            height: 100vh;
            width: 100vw;
            z-index: 99;
            background-color: rgba(50, 50, 50, 0.2);
        }

        .fade-enter-active,
        .fade-leave-active {
            transition: opacity 0.35s;
        }

        .fade-enter,
        .fade-leave-to {
            opacity: 0;
        }

        .slide-enter-active,
        .slide-leave-active {
            transition: left 0.35s ease-in-out;
        }

        .slide-enter-to,
        .slide-leave {
            left: 0;
        }

        .slide-enter,
        .slide-leave-to {
            left: -340px;
        }
    }
</style>
