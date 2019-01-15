import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import i18n from './i18n';
import VueNativeSock from 'vue-native-websocket';
import axios from 'axios';
import './bootstrap.ts';
import '@/util/LoggerConfig';
import Logger from 'js-logger';

const devMode: boolean = process.env.NODE_ENV === 'development';
Vue.config.productionTip = false;
Vue.config.silent = !devMode;
Vue.config.devtools = devMode;

let websocketUrl: string;
const isSecure: boolean = location.protocol === 'https:';

if (devMode) {
    Logger.get('Main').log('Running in Development Mode');
    const loc = window.location;
    axios.defaults.withCredentials = true;
    axios.defaults.baseURL = `${loc.protocol}//${loc.hostname}:${process.env.VUE_APP_BACKEND_PORT}`;
    websocketUrl = isSecure ? `wss://${loc.hostname}:${process.env.VUE_APP_BACKEND_PORT}/ws/` :
        `ws://${loc.hostname}:${process.env.VUE_APP_BACKEND_PORT}/ws/`;
} else {
    websocketUrl = isSecure ? 'wss://' + location.host + '/ws/' : 'ws://' + location.host + '/ws/';
}

Vue.use(VueNativeSock, websocketUrl, {
    store,
    format: 'json',
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 2000,
});

(window as any).vm = new Vue({
    router,
    store,
    i18n,
    render: (h) => h(App),
}).$mount('#app');
