import Vue from 'vue';
import Router from 'vue-router';
import routes from './routes';
import {loadLanguageAsync, possibleLanguages} from '@/i18n';

Vue.use(Router);

const router = new Router({
    mode: 'history',
    base: process.env.BASE_URL,
    routes,
});

// soon to be removed/refactored
router.beforeEach((to, from, next) => {
    const lang: string = (to.query.lang as string);
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

export default router;
