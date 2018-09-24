import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import './assets/scss/main.scss';
import i18n from './i18n';
import './bootstrap.ts';

const devMode: boolean = process.env.NODE_ENV === 'development';
Vue.config.productionTip = devMode;
Vue.config.silent = !devMode;
Vue.config.devtools = devMode;

(window as any).vm = new Vue({
    router,
    store,
    i18n,
    render: (h) => h(App),
}).$mount('#app');
