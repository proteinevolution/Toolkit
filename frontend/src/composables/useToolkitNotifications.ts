import { NotificationsOptions, useNotification } from '@kyvg/vue3-notification';
import { useWebNotification } from '@vueuse/core';
import { isNonNullable } from '@/util/nullability-helpers';

interface TKNotificationsOptions extends NotificationsOptions {
    title: string;
    text?: string;
    useBrowserNotifications?: boolean;
    onClick?: () => void;
}

export function useToolkitNotifications() {
    const { notify } = useNotification();

    const alert = (options: TKNotificationsOptions | string, type?: string) => {
        const newOptions: TKNotificationsOptions =
            typeof options === 'string'
                ? {
                      title: '',
                      text: options,
                      useBrowserNotifications: true,
                  }
                : options;

        if (type) {
            newOptions.type = type;
        }

        notify(newOptions);

        if (newOptions.useBrowserNotifications) {
            const { isSupported, show, close, onClick } = useWebNotification({
                title: newOptions.title,
                body: newOptions.text ?? '',
                icon: (document.getElementById('tk-favicon') as HTMLLinkElement | null)?.href ?? '',
            });
            if (isNonNullable(newOptions.onClick)) {
                onClick.on(newOptions.onClick);
            }

            // Only show browser notifications if the page is not visible
            if (isSupported.value && document.hidden) {
                show();
                setTimeout(close, 5000);
            }
        }
    };

    return { alert };
}
