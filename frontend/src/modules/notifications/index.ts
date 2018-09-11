import {VueConstructor} from 'vue/types/vue';
import Vue from 'vue';
import VueNotifications from 'vue-notification';
import velocity from 'velocity-animate';
import {TKNotificationOptions} from './types';

Vue.use(VueNotifications, {velocity});

const Notifications = {
    install(vconst: VueConstructor, args: any = {}) {
        vconst.prototype.$alert = (params: TKNotificationOptions | string, type?: string) => {
            const newParams: TKNotificationOptions = (typeof params === 'string') ?
                {
                    title: '',
                    text: params,
                    useBrowserNotifications: false,
                } : params;

            if (type) {
                newParams.type = type;
            }
            Vue.notify(newParams);

            if (args.allowBrowserNotification) {
                // TODO
            }
        };
    },
};

export default Notifications;
