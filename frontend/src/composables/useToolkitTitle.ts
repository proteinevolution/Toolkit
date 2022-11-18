import { isRef, onBeforeUnmount, Ref, ref, watchEffect } from 'vue';
import { createGlobalState, useTitle } from '@vueuse/core';
import { isNonNullable } from '@/util/nullability-helpers';

export const useGlobalTitleState = createGlobalState(() => {
    const base = document.title;
    const alert = ref(false);
    const title = useTitle();

    const refreshTitle = (titleText: string | undefined) => {
        title.value = `${alert.value ? '(*) ' : ''}${isNonNullable(titleText) ? `${titleText} | ` : ''}${base}`;
    };

    return { alert, refreshTitle };
});

export default function useToolkitTitle(htmlTitle?: Ref<string | undefined> | string) {
    const { alert, refreshTitle } = useGlobalTitleState();

    watchEffect(() => refreshTitle(isRef(htmlTitle) ? htmlTitle.value : htmlTitle));

    // remove title star on focus
    const removeAlertOnVisibility = () => {
        if (document.visibilityState === 'visible') {
            alert.value = false;
        }
    };

    document.addEventListener('visibilitychange', removeAlertOnVisibility);

    onBeforeUnmount(() => {
        document.removeEventListener('visibilitychange', removeAlertOnVisibility);
        refreshTitle(undefined);
    });
}
