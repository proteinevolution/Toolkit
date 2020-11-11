import 'es6-promise/auto';
import moment from 'moment';
import Vue from 'vue';
import BootstrapVue from 'bootstrap-vue';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import VueClipboard from 'vue-clipboard2';
import VueTour from 'vue-tour';

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
Vue.use(VueTour);
