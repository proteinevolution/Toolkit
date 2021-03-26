<template>
    <div class="d-flex mt-3 mt-md-5 justify-content-center">
        <b-col md="9"
               class="tile-main-container">
            <b-row>
                <b-col class="tile-image d-none d-md-flex" />
                <b-col class="update-section col-sm-7 col-md-6 col-lg-7 col-xl-7 px-4 py-4">
                    <h4>Recent Updates</h4>
                    <div v-for="update in updates"
                         :key="update.date"
                         class="update-item"
                         @click="launchUpdatesModal">
                        <h6 v-text="moment(update.date).format('LL')"></h6>
                        <p v-html="update.html"></p>
                    </div>
                </b-col>

                <b-col class="quick-link-section col-sm-4 col-md-4 col-lg-3 py-4 px-sm-4 px-3">
                    <h4>Quick Links</h4>
                    <b-nav vertical>
                        <router-link v-for="quickLink in quickLinks"
                                     :key="quickLink.title"
                                     :to="quickLink.link"
                                     class="quick-link">
                            {{ quickLink.title }}
                            <i class="fa fa-angle-right"></i>
                        </router-link>
                    </b-nav>
                </b-col>
            </b-row>
        </b-col>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import updates from '@/i18n/lang/updates';
import EventBus from '@/util/EventBus';

export default Vue.extend({
    name: 'UpdatesSection',
    data() {
        return {
            quickLinks: [{
                title: 'HHpred',
                link: 'tools/hhpred',
            }, {
                title: 'HHblits',
                link: 'tools/hhblits',
            }, {
                title: 'HHrepID',
                link: 'tools/hhrepid',
            }, {
                title: 'BLAST',
                link: 'tools/psiblast',
            }, {
                title: 'PCOILS',
                link: 'tools/pcoils',
            }, {
                title: 'CLANS',
                link: 'tools/clans',
            }, {
                title: 'MAFFT',
                link: 'tools/mafft',
            }, {
                title: 'Quick2D',
                link: 'tools/quick2d',
            }, {
                title: 'MMseqs2',
                link: 'tools/mmseqs2',
            }],
        };
    },
    computed: {
        updates() {
            return updates.slice(0, 3);
        },
    },
    methods: {
        launchUpdatesModal() {
            EventBus.$emit('show-modal', {id: 'updates'});
        },
    },
});
</script>

<style lang="scss" scoped>
.tile-main-container {
  background-color: $white;
  border: 1px solid $tk-light-gray;
  border-radius: $global-radius;
  box-shadow: 1px 1px 2px $tk-light-gray;

  h4 {
    white-space: nowrap; //stop headers from breaking text
    color: $primary;
    font-size: 1.1em;
    font-weight: bold;
    margin-bottom: 0.9rem;
  }

  .h4-padding-left {
    margin-left: 0.2em;
    @media (max-width: 575px) {
      margin-left: 0.4em;
    }
  }

  .tile-image {
    border-top-left-radius: $global-radius;
    border-bottom-left-radius: $global-radius;
    opacity: 0.85;
    background: url(../../assets/images/fold_galaxy.png) no-repeat center;
    background-size: cover;
  }

  .update-section .update-item {
    cursor: pointer;

    h6 {
      color: $primary;
      margin-bottom: 0.25em;
    }

    p {
      color: $tk-darker-gray;
      font-size: 0.8em;
    }

    @media (max-width: 300px) {
      width: 100%;
    }
  }

  .quick-link-section {
    border-top: 1px dashed $tk-light-gray;

    @media (min-width: 334px) {
      border-left: 1px dashed $tk-light-gray;
      border-top: none;
    }

    @media (width: 333px) {
      border-top: none;
      border-left: none;
    }

    @media (max-width: 332px) {
      border-left: none;
      border-top: 1px dashed $tk-light-gray;
    }

    .quick-link {
      color: $tk-darker-gray;
      display: flex;
      justify-content: space-between;
      text-decoration: none;
      padding-right: 0.2em;
      padding-left: 0.2em;
      max-width: 100%;

      @include media-breakpoint-up(lg) {
        max-width: 170px;
        padding: 0.15rem
      }

      @include media-breakpoint-up(sm) {
        max-width: 100%;
        padding: 0.15rem
      }

      @media (max-width: 575px) {
        border-radius: 0px;
        padding: 0.5em;
        border-top: 1px solid #dadce0;
      }

      i {
        font-size: 1.4em;
        color: $primary;
      }
    }
  }

  .quick-link-section a:hover {
    // nearly white color
    background: #F8F8F8;
    color: $primary;
    border-radius: 2px;
  }

}
</style>
