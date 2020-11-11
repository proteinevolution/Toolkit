import Vue from 'vue';
import EventBus from '@/util/EventBus';

const TourMixin = Vue.extend({
    name: 'TourMixin',
    data() {
        return {
            steps: [
                {
                    target: '[data-v-step="toolbar"]',
                    header: {
                        title: 'Toolbar',
                    },
                    content: `These are the different tools you can use`,
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
                    target: '[data-v-step="tool"]',
                    content: `Let's checkout this tool.`,
                    params: {
                        enableScrolling: false,
                    },
                },
                {
                    target: '[data-v-step="paste"]',
                    content: 'Paste an example.',
                    params: {
                        placement: 'left',
                    },
                    before: (type: string) => new Promise<void>((resolve) => {
                        if (this.$route.path !== '/tools/hhblits') {
                            this.$router.push('/tools/hhblits');
                        }
                        if (type === 'previous') {
                            EventBus.$emit('change-tool-tab', 0);
                        }
                        const poll = setInterval(() => {
                            if (document.querySelector('[data-v-step="paste"]')) {
                                clearInterval(poll);
                                resolve();
                            }
                        }, 250);
                    }),
                },
                {
                    target: '.tour-tab-Parameters',
                    content: `Change tool parameters`,
                    params: {
                        placement: 'right',
                    },
                    before: () => new Promise<void>((resolve) => {
                        EventBus.$emit('remote-trigger-paste-example');
                        resolve();
                    }),
                },
                {
                    target: '[data-v-step="submit"]',
                    content: `Submit your job`,
                    before: () => new Promise<void>((resolve) => {
                        EventBus.$emit('change-tool-tab', 1);
                        resolve();
                    }),
                },
            ],
        };
    },
    computed: {
        tour(): any {
            return this.$tours['toolkitTour'];
        },
    },
    watch: {
        '$route.path'(path: string): void {
            if (this.tour.currentStep === 1 && path === '/tools/hhblits') {
                this.tour.nextStep();
            } else {
                this.tour.stop();
            }
        },
    },
});

export default TourMixin;
