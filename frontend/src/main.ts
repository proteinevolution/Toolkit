import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import i18n from './i18n';
import axios from 'axios';
import '@/util/LoggerConfig';
import Logger from 'js-logger';
import { pinia } from '@/stores';
import BootstrapVue from 'bootstrap-vue';
import Notifications from '@kyvg/vue3-notification';
import velocity from 'velocity-animate';
import VueTour from 'v3-tour';

const app = createApp(App);
(window as any).vm = app;

if (import.meta.env.PROD) {
    app.config.errorHandler = () => null;
    app.config.warnHandler = () => null;
} else if (import.meta.env.DEV) {
    Logger.get('Main').log('Running in Development Mode');
    axios.defaults.withCredentials = true;
}

app.use(router);
app.use(pinia);
app.use(i18n);

app.use(BootstrapVue);
app.use(Notifications, { velocity });
app.use(VueTour);

app.mount('#app');
