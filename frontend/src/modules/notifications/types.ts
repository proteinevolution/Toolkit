import {NotificationOptions} from 'vue-notification';

declare module 'vue/types/vue' {
    // Global properties can be declared
    // on the `VueConstructor` interface
    interface VueConstructor {
        notify: (options: TKNotificationOptions | string) => void;
    }
}

export interface TKNotificationOptions extends NotificationOptions {
    useBrowserNotifications?: boolean;
}
