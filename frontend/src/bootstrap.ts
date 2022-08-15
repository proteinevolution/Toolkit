import BootstrapVue from 'bootstrap-vue';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import Vue from 'vue';
import {PiniaVuePlugin} from 'pinia';

Vue.use(BootstrapVue);
Vue.use(Notifications, {
    browserNotifications: {
        enabled: true,
        timeout: 5000,
        onlyIfHidden: true,
    },
});
Vue.use(TitleManager);
Vue.use(PiniaVuePlugin);
