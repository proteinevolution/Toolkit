import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import i18n from './i18n';
import VueNativeSock from 'vue-native-websocket';
import axios from 'axios';
import '@/util/LoggerConfig';
import Logger from 'js-logger';
import { pinia } from '@/stores';
import { useRootStoreWithout } from '@/stores/root';
import BootstrapVue from 'bootstrap-vue';
import Notifications from '@/modules/notifications';
import TitleManager from '@/modules/title_manager';
import VueTour from 'vue-tour';

const app = createApp(App);
(window as any).vm = app;

if (import.meta.env.PROD) {
    app.config.errorHandler = () => null;
    app.config.warnHandler = () => null;
} else if (import.meta.env.DEV) {
    Logger.get('Main').log('Running in Development Mode');
    axios.defaults.withCredentials = true;
}

const isSecure: boolean = location.protocol === 'https:';
const websocketUrl: string = isSecure ? 'wss://' + location.host + '/ws/' : 'ws://' + location.host + '/ws/';
app.use(VueNativeSock, websocketUrl, {
    /* TODO: WebSocket events will also be accessible in the store which passes it to global prototype $socket.
        This is not the best pattern and should be replaced. Think about using useWebSocket once we are on Vue3. */
    store: useRootStoreWithout(),
    passToStoreHandler: function (eventName: string, event: any) {
        // copied from https://github.com/likaia/vue-native-websocket-vue3/blob/master/src/socket-server/Observer.ts#L161
        if (!eventName.startsWith('SOCKET_')) {
            return;
        }
        let target = eventName.toUpperCase();
        let msg = event;
        // data exists and the data is in json format
        if (event.data) {
            // Convert data from json string to json object
            msg = JSON.parse(event.data);
            if (msg.mutation) {
                target = msg.mutation;
            }
        }
        // send to pinia
        this.store[target](msg);
    },
    format: 'json',
    reconnection: true,
    reconnectionAttempts: 5,
    reconnectionDelay: 2000,
});

app.use(router);
app.use(pinia);
app.use(i18n);

app.use(BootstrapVue);
app.use(Notifications, {
    browserNotifications: {
        enabled: true,
        timeout: 5000,
        onlyIfHidden: true,
    },
});
app.use(TitleManager);
app.use(VueTour);

app.mount('#app');
