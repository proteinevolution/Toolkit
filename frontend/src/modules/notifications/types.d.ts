// Important: import Vue from 'vue';
import Vue from 'vue';

import {NotificationOptions} from 'vue-notification';

declare module 'vue/types/vue' {
    // Global properties can be declared
    // on the `VueConstructor` interface
    interface VueConstructor {
        notify: (options: NotificationOptions | string) => void;
    }
}

declare module 'vue/types/vue' {
    interface Vue {
        $alert: (options: NotificationOptions | string, body?: string, type?: string) => void;
    }
}

export interface TKNotificationOptions extends NotificationOptions {
    text: string;
    body?: string;
    useBrowserNotifications?: boolean;
    onClick?: () => void;
}
