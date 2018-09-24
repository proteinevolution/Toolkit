import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import './assets/scss/main.scss';
import i18n from './i18n';
import './bootstrap.ts';
import MsaViewer from '@/modules/msaviewer';

Vue.config.productionTip = false;

(window as any).vm = new Vue({
    router,
    store,
    i18n,
    render: (h) => h(App),
}).$mount('#app');

Vue.use(MsaViewer);
