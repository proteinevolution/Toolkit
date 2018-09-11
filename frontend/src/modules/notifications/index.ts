import {VueConstructor} from 'vue/types/vue';
import Vue from 'vue';
import VueNotifications from 'vue-notification';
import velocity from 'velocity-animate';
import {TKNotificationOptions} from './types';

Vue.use(VueNotifications, {velocity});

const Notifications = {
    install(vconst: VueConstructor, args: any = {}) {
        // override notify method to allow browser notifications
        if (args.allowBrowserNotification) {
            vconst.prototype.$notify = (params: TKNotificationOptions | string) => {
                Vue.notify(params);

                const newParams: TKNotificationOptions = (typeof params === 'string') ?
                    {
                        title: '',
                        text: params,
                        useBrowserNotifications: false,
                    } : params;

                // TODO show browser notification if necessary
            };
        }
    },
};

export default Notifications;
