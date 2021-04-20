import Vue from 'vue';
import EventBus from '@/util/EventBus';
import { Tour } from 'vue-tour';

const TourMixin = Vue.extend({
    name: 'TourMixin',
    data() {
        return {
            steps: [
                {
                    target: '[data-v-step="tool-bar"]',
                    header: {
                        title: 'Toolbar',
                    },
                    content: this.$t('tour.content.toolBar'),
                    params: {
                        enableScrolling: false,
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'start' && this.$route.path !== '/') {
                            this.$router.push('/');
                        }
                        resolve();
                    }),
                },
                {
                    // TODO: Fix issue where tour element is placed wrong when going back from step
                    // 4 to step 2. This problem occurs when the tour element is placed before
                    // the image is loaded. The image then pushes the search bar down, away from
                    // the tour element
                    target: '[data-v-step="search-bar"]',
                    content: this.$t('tour.content.searchBar'),
                    params: {
                        enableScrolling: false,
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'previous' && this.$route.path !== '/') {
                            this.$router.push('/');
                        }
                        const poll = setInterval(() => {
                                if (document.querySelector('[data-v-step="search-bar"]')) {
                                    clearInterval(poll);
                                    resolve();
                                }
                        }, 100);
                    }),
                },
                {
                    // TODO: make sure that the "Search" Tab is selected. Otherwise
                    // the tooltip won't show the HHpred tool
                    target: '[data-v-step="tool"]',
                    content: this.$t('tour.content.tool'),
                    params: {
                        enableScrolling: false,
                    },
                },
                {
                    target: '[data-v-step="input"]',
                    content: this.$t('tour.content.input'),
                    params: {
                        enableScrolling: false,
                        placement: 'top'
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (this.$route.path !== '/tools/hhpred') {
                            this.$router.push('/tools/hhpred');
                        }
                        if (type === 'previous') {
                            EventBus.$emit('change-tool-tab', 0);
                        }
                        const poll = setInterval(() => {
                            if (document.querySelector('[data-v-step="input"]')) {
                                clearInterval(poll);
                                resolve();
                            }
                        }, 100);
                    }),
                },
                {
                    target: '[data-v-step="paste"]',
                    content: this.$t('tour.content.paste'),
                    params: {
                        placement: 'left',
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'previous') {
                            EventBus.$emit('change-tool-tab', 0);
                        }
                        resolve();
                    }),
                },
                {
                    target: '[data-v-step="structural-domain-database"]',
                    content: this.$t('tour.content.structuralDomainDatabase'),
                    params: {
                        placement: 'top',
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'next') {
                            EventBus.$emit('remote-trigger-paste-example');
                        }
                        resolve();
                    }),
                },
                {
                    target: '[data-v-step="proteomes"]',
                    content: this.$t('tour.content.proteomes'),
                    params: {
                        placement: 'top',
                    },
                },
                {
                    target: '.tour-tab-Parameters',
                    content: this.$t('tour.content.parametersTab'),
                    params: {
                        placement: 'right',
                    },
                },
                {
                    // since every tab has its own buttons, we have to select the ones in
                    // the active tab
                    target: '.tab-pane[aria-hidden=false] [data-v-step="job-id"]',
                    content: this.$t('tour.content.jobId'),
                },
                {
                    target: '.tab-pane[aria-hidden=false] [data-v-step="submit"]',
                    content: this.$t('tour.content.submit'),
                },
                {
                    target: '[data-v-step="job-manager"]',
                    content: this.$t('tour.content.jobManager'),
                    params: {
                        placement: 'right'
                    }
                },
            ],
            options: {
                labels: {
                    buttonSkip: this.$t('tour.labels.buttonSkip'),
                    buttonPrevious: this.$t('tour.labels.buttonPrevious'),
                    buttonNext: this.$t('tour.labels.buttonNext'),
                    buttonStop: this.$t('tour.labels.buttonStop'),
                }
            }
        };
    },
    computed: {
        tour(): Tour {
            return this.$tours['toolkitTour'];
        },
    },
    watch: {
        '$route.path'(path: string): void {
            console.log(this.tour.currentStep)
            if (this.tour.currentStep === 2 && path === '/tools/hhpred') {
                this.tour.nextStep();
            } else {
                this.tour.stop();
            }
        },
    },
});

export default TourMixin;
