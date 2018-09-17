import BootstrapVue from 'bootstrap-vue';
import VueJsModal from 'vue-js-modal';
import Notifications from '@/modules/notifications';
import moment from 'moment';
import VueParticles from 'vue-particles';
import Vue from 'vue';

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
Vue.prototype.moment = moment;
Vue.use(VueParticles);
