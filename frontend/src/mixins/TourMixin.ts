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
                    target: '[data-v-step="tool"]',
                    content: this.$t('tour.content.tool'),
                    params: {
                        enableScrolling: false,
                    },
                    before: () => new Promise<void>((resolve) => {
                        EventBus.$emit('select-nav-bar-section', 'search');
                        // Give the navBar a moment to switch tabs before the message
                        // can be displayed
                        setTimeout(resolve, 20);
                    }),
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
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'previous') {
                            EventBus.$emit('change-tool-tab', 0);
                        }
                        resolve();
                    }),
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
                    before: () => new Promise<void>((resolve) => {
                        EventBus.$emit('change-tool-tab', 1);
                        resolve();
                    }),
                },
                {
                    target: '.tab-pane[aria-hidden=false] [data-v-step="submit"]',
                    content: this.$t('tour.content.submit'),
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (type === 'previous') {
                            if (this.$route.path !== '/tools/hhpred') {
                                this.$router.push('/tools/hhpred');
                            }
                            const poll = setInterval(() => {
                                if (document.querySelector('[data-v-step="submit"]')) {
                                    clearInterval(poll);
                                    resolve();
                                }
                            }, 100);
                        } else {
                            resolve()
                        }
                    })
                },
                {
                    target: '[data-v-step="job-list"]',
                    content: this.$t('tour.content.jobList'),
                    params: {
                        placement: 'right'
                    }
                },
                {
                    target: '[data-v-step="job-manager"]',
                    content: this.$t('tour.content.jobManager'),
                    params: {
                        placement: 'top'
                    },
                    before: () => new Promise<void>((resolve) => {
                        if (this.$route.path !== '/jobmanager') {
                            this.$router.push('/jobmanager');
                        }
                        const poll = setInterval(() => {
                            if (document.querySelector('[data-v-step="job-manager"]')) {
                                clearInterval(poll);
                                resolve();
                            }
                        }, 100);
                    })
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
            if (this.tour.currentStep === 2 && path === '/tools/hhpred') {
                this.tour.nextStep();
            } else if (this.tour.currentStep === 10 && path === '/jobmanager') {
                this.tour.nextStep();
            }else {
                this.tour.stop();
            }
        },
    },
});

export default TourMixin;
