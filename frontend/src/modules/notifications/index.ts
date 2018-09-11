import {VueConstructor} from 'vue/types/vue';
import Vue from 'vue';
import VueNotifications from 'vue-notification';
import velocity from 'velocity-animate';
import {TKNotificationOptions} from './types';

Vue.use(VueNotifications, {velocity});

const Notifications = {
    install(vconst: VueConstructor, args: any = {}) {
        vconst.prototype.$alert = (params: TKNotificationOptions | string, body?: string, type?: string) => {
            const newParams: TKNotificationOptions = (typeof params === 'string') ?
                {
                    title: '',
                    text: params,
                    body,
                    useBrowserNotifications: true, // TODO
                } : params;

            if (type) {
                newParams.type = type;
            }
            Vue.notify(newParams);

            if (args.browserNotifications.enabled) {
                const notifOptions = {
                    body,
                    icon: '',
                };
                const favicon: HTMLElement | null = document.getElementById('tk-favicon');
                if (favicon) {
                    notifOptions.icon = (favicon as HTMLLinkElement).href;
                }
                if ('Notification' in window && newParams.useBrowserNotifications &&
                    (!args.browserNotifications.onlyIfHidden || document.hidden)) {

                    const browserNotification = (window as any).Notification;
                    if (browserNotification.permission === 'granted') {
                        const n = new Notification(newParams.text, notifOptions);
                        if (newParams.onClick) {
                            n.onclick = newParams.onClick;
                        }
                        setTimeout(() => {
                            n.close();
                        }, args.browserNotifications.timeout || 5000);
                    } else if (browserNotification.permission !== 'denied') {
                        browserNotification.requestPermission((permission: string) => {
                            if (permission === 'granted') {
                                notifOptions.body = 'You will now receive updates on your jobs over notifications.';
                                new Notification('Thank You!', notifOptions);
                            }
                        });
                    }
                }
            }
        };
    },
};

export default Notifications;
