<template>
    <div id="offscreenMenu"
         class="offscreen-menu-wrapper">
        <transition name="fade">
            <div v-if="isOpen"
                 class="offscreen-backdrop"
                 @click="close"></div>
        </transition>
        <transition name="slide">
            <div v-if="isOpen"
                 class="offscreen-menu">
                <transition mode="out-in">
                    <b-nav v-if="!selectedSection"
                           key="top-level"
                           vertical
                           class="mt-2">
                        <b-nav-item key="home"
                                    to="/"
                                    @click="close">
                            Home
                        </b-nav-item>
                        <b-nav-item v-if="isAdmin"
                                    class="section-link"
                                    @click="switchToAdminView()">
                            Admin
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
                    <b-nav v-else
                           key="bottom-level"
                           vertical
                           class="mt-2">
                        <b-nav-item key="back"
                                    @click="selectedSection = ''">
                            <i class="fa fa-angle-left mr-2"></i>
                            {{ $t(`back`) }}
                        </b-nav-item>
                        <b-nav-item v-for="tool in displayedTools"
                                    :key="tool.name"
                                    :to="'/tools/' + tool.name"
                                    @click="close">
                            {{ tool.longname }}
                        </b-nav-item>
                    </b-nav>
                </transition>

                <JobList @click="close" />

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
import {User} from '@/types/toolkit/auth';
import {mapStores} from 'pinia';
import {useToolsStore} from '@/stores/tools';
import {useRootStore} from '@/stores/root';
import {useAuthStore} from '@/stores/auth';

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
            return this.rootStore.offscreenMenuShow;
        },
        tools(): Tool[] {
            return this.toolsStore.tools;
        },
        displayedTools(): Tool[] {
            return this.tools.filter((tool: Tool) => tool.section === this.selectedSection);
        },
        user(): User | null {
            return this.authStore.user;
        },
        isAdmin(): boolean {
            return this.user !== null && this.user.isAdmin;
        },
        ...mapStores(useRootStore, useAuthStore, useToolsStore),
    },
    methods: {
        close(): void {
            this.rootStore.offscreenMenuShow = false;
            this.selectedSection = '';
        },
        switchToAdminView(): void {
            this.close();
            this.$router.push("/admin");
        }
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
      background-color: $tk-lighter-gray;
    }

    .offscreen-menu-close:hover {
      background: $tk-light-gray;
    }

    .nav-link {
      color: $tk-darker-gray;
    }

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
