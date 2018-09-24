import BootstrapVue from 'bootstrap-vue';
import VueJsModal from 'vue-js-modal';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import moment from 'moment';
import VueParticles from 'vue-particles';
import VueClipboard from 'vue-clipboard2';
import Vue from 'vue';
import 'es6-promise/auto';

Vue.use(BootstrapVue);
Vue.use(VueJsModal, {
    dialog: true,
    dynamic: true,
});
Vue.use(Notifications, {
    browserNotifications: {
        enabled: true,
        timeout: 5000,
        onlyIfHidden: true,
    },
});
Vue.use(TitleManager);
Vue.prototype.moment = moment;
Vue.use(VueParticles);
Vue.use(VueClipboard);
