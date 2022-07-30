import Vue from 'vue';
import Router from 'vue-router';
import routes from './routes';
import Logger from 'js-logger';

const logger = Logger.get('router');

Vue.use(Router);

const router = new Router({
    mode: 'history',
    base: import.meta.env.BASE_URL,
    routes,
});

// fallback for hash mode
router.beforeEach((to, from, next) => {
    // Redirect if fullPath begins with a hash (ignore hashes later in path)
    if (to.fullPath.substr(0, 2) === '/#') {
        const path = to.fullPath.substr(2);
        logger.info('hash mode detected. Redirecting to: ' + path);
        next(path);
        return;
    }
    next();
});

export default router;
