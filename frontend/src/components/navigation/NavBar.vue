<template>
    <div class="navbar-container">
        <b-navbar toggleable="md"
                  type="light">
            <b-collapse id="nav_collapse"
                        is-nav>
                <b-row>
                    <b-col cols="auto">
                        <b-navbar-nav class="upper-nav">
                            <b-nav-item v-for="section in sections"
                                        :key="section"
                                        :class="[section === selectedSection && $route.name !== 'admin' ? 'active' : '']"
                                        @click="selectSection(section)">
                                {{ $t(`tools.sections.${section}.title`) }}
                            </b-nav-item>
                            <b-nav-item v-if="isAdmin"
                                        :class="[$route.name === 'admin' ? 'active' : '']"
                                        to="/admin">
                                Admin
                            </b-nav-item>
                        </b-navbar-nav>

                        <transition-group data-v-step="tool-bar"
                                          name="list-complete"
                                          tag="ul"
                                          class="navbar-nav lower-nav"
                                          :style="{borderTopColor: sectionColor}">
                            <b-nav-item v-for="(tool, index) in displayedTools"
                                        :key="tool.name"
                                        v-b-tooltip.hover.bottom
                                        class="list-complete-item"
                                        :data-v-step="index === 1 ? 'tool' : ''"
                                        :to="'/tools/' + tool.name"
                                        :title="tool.description">
                                {{ tool.longname }}
                            </b-nav-item>
                        </transition-group>
                    </b-col>
                </b-row>
            </b-collapse>
        </b-navbar>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {Tool} from '@/types/toolkit/tools';
import {Job} from '@/types/toolkit/jobs';
import {sectionColors, sections} from '@/conf/ToolSections';
import {User} from '@/types/toolkit/auth';
import {mapStores} from 'pinia';
import {useToolsStore} from '@/stores/tools';
import {useJobsStore} from '@/stores/jobs';
import {useAuthStore} from '@/stores/auth';

export default Vue.extend({
    name: 'NavBar',
    data() {
        return {
            userSelectedSection: '',
            defaultSelectedSection: sections[0],
            sectionColors,
            sections,
        };
    },
    computed: {
        displayedTools(): Tool[] {
            return this.tools.filter((tool: Tool) => tool.section === this.selectedSection);
        },
        tools(): Tool[] {
            return this.toolsStore.tools;
        },
        jobs(): Job[] {
            return this.jobsStore.jobs;
        },
        sectionColor(): string {
            const index = this.sections.indexOf(this.selectedSection) % this.sections.length;
            return this.sectionColors[index];
        },
        selectedSection(): string {
            return this.userSelectedSection ? this.userSelectedSection : this.defaultSelectedSection;
        },
        user(): User | null {
            return this.authStore.user;
        },
        isAdmin(): boolean {
            return this.user !== null && this.user.isAdmin;
        },
        ...mapStores(useAuthStore, useToolsStore, useJobsStore),
    },
    watch: {
        '$route.params': {
            immediate: true,
            handler() {
                // clear user selection to select correct tool/group upon programmatic routing
                this.userSelectedSection = '';
                let toolName = '';
                if (this.$route.params.toolName) {
                    toolName = this.$route.params.toolName;
                } else {
                    const currentJob: Job | undefined =
                        this.jobs.find((job: Job) => job.jobID === this.$route.params.jobID);
                    if (currentJob) {
                        toolName = currentJob.tool;
                    }
                }
                if (toolName) {
                    const matchingTools = this.tools.filter((tool: Tool) => tool.name === toolName
                        && sections.includes(tool.section));
                    if (matchingTools.length > 0) {
                        this.defaultSelectedSection = matchingTools[0].section;
                    } else {
                        this.defaultSelectedSection = sections[0];
                    }
                }
            },
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
