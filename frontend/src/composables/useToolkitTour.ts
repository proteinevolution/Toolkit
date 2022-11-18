import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { useEventBus } from '@vueuse/core';

export default function useToolkitTour() {
    const { t } = useI18n();
    const router = useRouter();
    const route = useRoute();
    const changeToolTabBus = useEventBus<number>('change-tool-tab');
    const remoteTriggerPasteExampleBus = useEventBus<void>('remote-trigger-paste-example');
    const selectNavBarSectionBus = useEventBus<string>('select-nav-bar-section');

    const steps = [
        {
            target: '[data-v-step="tool-bar"]',
            header: {
                title: 'Tool Bar',
            },
            content: t('tour.content.toolBar'),
            params: {
                enableScrolling: false,
            },
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (type === 'start' && route.path !== '/') {
                        router.push('/');
                    }
                    resolve();
                }),
        },
        {
            target: '[data-v-step="search-bar"]',
            header: {
                title: 'Search Box',
            },
            content: t('tour.content.searchBar'),
            params: {
                enableScrolling: false,
            },
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (type === 'previous' && route.path !== '/') {
                        router.push('/');
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
            content: t('tour.content.tool'),
            params: {
                enableScrolling: false,
            },
            before: () =>
                new Promise<void>((resolve) => {
                    selectNavBarSectionBus.emit('search');
                    // Give the navBar a moment to switch tabs before the message
                    // can be displayed
                    setTimeout(resolve, 20);
                }),
        },
        {
            target: '[data-v-step="help-modal"]',
            header: {
                title: 'Help Pages',
            },
            content: t('tour.content.help'),
            params: {
                enableScrolling: false,
                placement: 'right',
            },
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (route.path !== '/tools/hhpred') {
                        router.push('/tools/hhpred');
                    }
                    if (type === 'previous') {
                        changeToolTabBus.emit(0);
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
            target: '[data-v-step="input"]',
            header: {
                title: 'Input Field',
            },
            content: t('tour.content.input'),
            params: {
                enableScrolling: false,
                placement: 'top',
            },
        },
        {
            target: '[data-v-step="paste"]',
            content: t('tour.content.paste'),
            params: {
                placement: 'left',
            },
        },
        {
            target: '[data-v-step="structural-domain-database"]',
            content: t('tour.content.structuralDomainDatabase'),
            params: {
                placement: 'top',
            },
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (type === 'next') {
                        remoteTriggerPasteExampleBus.emit();
                    }
                    resolve();
                }),
        },
        {
            target: '[data-v-step="proteomes"]',
            content: t('tour.content.proteomes'),
            params: {
                placement: 'top',
            },
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (type === 'previous') {
                        changeToolTabBus.emit(0);
                    }
                    resolve();
                }),
        },
        {
            target: '.tour-tab-Parameters',
            header: {
                title: 'Parameters Tab',
            },
            content: t('tour.content.parametersTab'),
            params: {
                placement: 'right',
            },
        },
        {
            // since every tab has its own buttons, we have to select the ones in
            // the active tab
            target: '.tab-pane[aria-hidden=false] [data-v-step="job-id"]',
            content: t('tour.content.jobId'),
            before: () =>
                new Promise<void>((resolve) => {
                    changeToolTabBus.emit(1);
                    resolve();
                }),
        },
        {
            target: '.tab-pane[aria-hidden=false] [data-v-step="submit"]',
            content: t('tour.content.submit'),
            before: (type: string) =>
                new Promise<void>((resolve) => {
                    if (type === 'previous') {
                        if (route.path !== '/tools/hhpred') {
                            router.push('/tools/hhpred');
                        }
                        const poll = setInterval(() => {
                            if (document.querySelector('[data-v-step="submit"]')) {
                                clearInterval(poll);
                                resolve();
                            }
                        }, 100);
                    } else {
                        resolve();
                    }
                }),
        },
        {
            target: '[data-v-step="job-list"]',
            header: {
                title: 'Job List',
            },
            content: t('tour.content.jobList'),
            params: {
                placement: 'top',
            },
            before: () =>
                new Promise<void>((resolve) => {
                    const poll = setInterval(() => {
                        if (document.querySelector('[data-v-step="job-list"]')) {
                            clearInterval(poll);
                            resolve();
                        }
                    }, 100);
                }),
        },
        {
            target: '[data-v-step="job-manager"]',
            header: {
                title: 'Job Manager',
            },
            content: t('tour.content.jobManager'),
            params: {
                placement: 'top',
            },
            before: () =>
                new Promise<void>((resolve) => {
                    if (route.path !== '/jobmanager') {
                        router.push('/jobmanager');
                    }
                    const poll = setInterval(() => {
                        if (document.querySelector('[data-v-step="job-manager"]')) {
                            clearInterval(poll);
                            resolve();
                        }
                    }, 100);
                }),
        },
    ];

    const options = {
        labels: {
            buttonSkip: t('tour.labels.buttonSkip'),
            buttonPrevious: t('tour.labels.buttonPrevious'),
            buttonNext: t('tour.labels.buttonNext'),
            buttonStop: t('tour.labels.buttonStop'),
        },
    };

    return { options, steps };
}
