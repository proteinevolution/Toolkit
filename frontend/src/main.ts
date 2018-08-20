import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import BootstrapVue from 'bootstrap-vue';
import './assets/scss/main.scss';
import {i18n, loadLanguageAsync, possibleLanguages} from './i18n';

Vue.use(BootstrapVue);

Vue.config.productionTip = false;

// soon to be removed/refactored
router.beforeEach((to, from, next) => {
    const lang = to.query.lang;
    if (lang !== undefined && lang !== from.query.lang) {
        if (possibleLanguages.includes(lang)) {
            // logger.debug('switching to language: ' + lang);
            loadLanguageAsync(lang)
                .then(() => next())
                .catch();
        } else {
            // logger.error('Trying to switch to unrecognized language: ' + lang);
            next();
        }
    } else {
        next();
    }
});

new Vue({
    router,
    store,
    i18n,
    render: (h) => h(App),
}).$mount('#app');
