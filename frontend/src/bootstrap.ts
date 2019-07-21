import BootstrapVue from 'bootstrap-vue';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import moment from 'moment';
import VueParticles from 'vue-particles';
import VueClipboard from 'vue-clipboard2';
import Sticky from 'vue-sticky-directive';
import Vue from 'vue';
import 'msa/dist/msa';
import 'es6-promise/auto';

Vue.use(BootstrapVue);
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
Vue.use(Sticky);
