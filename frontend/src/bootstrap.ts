import BootstrapVue from 'bootstrap-vue';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import moment from 'moment';
import VueClipboard from 'vue-clipboard2';
import Vue from 'vue';
import 'es6-promise/auto';
import VueTour from 'vue-tour';

Vue.use(VueTour);
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
Vue.use(VueClipboard);
