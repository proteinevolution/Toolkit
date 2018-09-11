import BootstrapVue from 'bootstrap-vue';
import VueJsModal from 'vue-js-modal';
import Notifications from '@/modules/notifications';
import Vue from 'vue';

Vue.use(BootstrapVue);
Vue.use(VueJsModal, {
    dialog: true,
    dynamic: true,
});
Vue.use(Notifications, {
    allowBrowserNotification: true,
});
