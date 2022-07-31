import IndexView from '../components/index/IndexView.vue';
import Loading from '../components/utils/Loading.vue';
import TimeoutView from '../components/utils/TimeoutView.vue';
import {Component, CreateElement, defineAsyncComponent, VNode, VNodeChildren, VNodeData} from 'vue';
import {Location, NavigationGuardNext, Route, RouteConfig} from 'vue-router';
import {authService} from '@/services/AuthService';

const ToolView = (): Promise<Component> => lazyLoadView(import('../components/tools/ToolView.vue'));
const JobView = (): Promise<Component> => lazyLoadView(import('../components/jobs/JobView.vue'));
const JobManagerView = (): Promise<Component> => lazyLoadView(import('../components/jobmanager/JobManagerView.vue'));
const AdminView = (): Promise<Component> => lazyLoadView(import('../components/admin/AdminView.vue'));
const NotFoundView = (): Promise<Component> => lazyLoadView(import('../components/utils/NotFoundView.vue'));

const routes: RouteConfig[] = [
    {
        path: '/',
        name: 'index',
        component: IndexView,
        meta: {
            showJobList: false,
        },
    },
    {
        path: '/tools/:toolName',
        name: 'tools',
        component: ToolView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/jobs/:jobID',
        name: 'jobs',
        component: JobView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/jobmanager',
        name: 'jobmanager',
        component: JobManagerView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/admin',
        name: 'admin',
        component: AdminView,
        meta: {
            showJobList: true,
        },
        beforeEnter: async (to: Route, from: Route, next: NavigationGuardNext): Promise<void> => {
            const user = await authService.fetchUserData();
            if (user != null && user.isAdmin) {
                next();
            } else {
                next({name: 'index'});
            }
        },
    },
    {
        path: '/verify/:nameLogin/:token',
        redirect: (to: Route): Location => {
            const {params} = to;
            params.action = 'verification';
            return {name: 'index', query: params};
        },
    },
    {
        path: '/reset-password/:nameLogin/:token',
        redirect: (to: Route): Location => {
            const {params} = to;
            params.action = 'resetPassword';
            return {name: 'index', query: params};
        },
    },
    {
        path: '/**',
        name: '404',
        component: NotFoundView,
        meta: {
            showJobList: true,
        },
    },
];

export default routes;

/*
REMARK: This function is taken from
https://github.com/chrisvfritz/vue-enterprise-boilerplate/blob/master/src/router/routes.js
and updated with
https://github.com/boydaihungst/vue-enterprise-boilerplate/blob/main/src/router/routes.ts
and just live tested. No guarantee on the functionality - replace with direct lazy-loading if something goes wrong.

---------------------- Original comments: ----------------------------

Lazy-loads view components, but with better UX. A loading view
will be used if the component takes a while to load, falling
back to a timeout view in case the page fails to load. You can
use this component to lazy-load a route with:

component: () => lazyLoadView(import('@views/my-view'))

NOTE: Components loaded with this strategy DO NOT have access
to in-component guards, such as beforeRouteEnter,
beforeRouteUpdate, and beforeRouteLeave. You must either use
route-level guards instead or lazy-load the component directly:

component: () => import('@views/my-view')
*/
export function lazyLoadView(AsyncView: Promise<typeof import ('*.vue')>): Promise<Component> {
    const AsyncHandler = defineAsyncComponent({
        loader: () => AsyncView,
        // A component to use while the component is loading.
        loadingComponent: Loading,
        // Delay before showing the loading component.
        // Default: 200 (milliseconds).
        delay: 400,
        // A fallback component in case the timeout is exceeded
        // when loading the component.
        errorComponent: TimeoutView,
        // Time before giving up trying to load the component.
        // Default: Infinity (milliseconds).
        timeout: 10000,
        /**
         *
         * @param {*} error Error message object
         * @param {*} retry A function that indicating whether the async component should retry when the loader promise rejects
         * @param {*} fail  End of failure
         * @param {*} attempts Maximum allowed retries number
         */
        onError(error, retry, fail, attempts) {
            if (error.message.match(/fetch/) && attempts <= 3) {
                // retry on fetch errors, 3 max attempts
                retry();
            } else {
                // Note that retry/fail are like resolve/reject of a promise:
                // one of them must be called for the error handling to continue.
                fail();
            }
        },
    });

    // TODO: with Vue3 replace this with what the template has
    return Promise.resolve({
        functional: true,
        render(h: CreateElement, {data, children}: { data: VNodeData, children: VNodeChildren }): VNode {
            // Transparently pass any props or children to the view component.
            return h(AsyncHandler, data, children);
        },
    });
}
