import IndexView from '../components/index/IndexView.vue';
import Loading from '../components/utils/Loading.vue';
import TimeoutView from '../components/utils/TimeoutView.vue';
import {Component, CreateElement, VNode, VNodeChildren, VNodeData} from 'vue';
import {Location, NavigationGuardNext, Route, RouteConfig} from 'vue-router';
import {authService} from '@/services/AuthService';

const ToolView = (): Promise<Component> => lazyLoadView(import(/* webpackChunkName: "tool" */ '../components/tools/ToolView.vue'));
const JobView = (): Promise<Component> => lazyLoadView(import(/* webpackChunkName: "job" */ '../components/jobs/JobView.vue'));
const JobManagerView = (): Promise<Component> => lazyLoadView(import(/* webpackChunkName: "jobmanager" */ '../components/jobmanager/JobManagerView.vue'));
const AdminView = (): Promise<Component> => lazyLoadView(import(/* webpackChunkName: "admin" */ '../components/admin/AdminView.vue'));
const NotFoundView = (): Promise<Component> => lazyLoadView(import(/* webpackChunkName: "404" */ '../components/utils/NotFoundView.vue'));

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
    const AsyncHandler = () => ({
        component: AsyncView,
        // A component to use while the component is loading.
        loading: Loading,
        // A fallback component in case the timeout is exceeded when loading the component.
        error: TimeoutView,
        // Delay before showing the loading component.
        delay: 400,
        // Time before giving up trying to load the component.
        timeout: 10000,
    } as Component);

    return Promise.resolve({
        functional: true,
        render(h: CreateElement, {data, children}: { data: VNodeData, children: VNodeChildren }): VNode {
            // Transparently pass any props or children to the view component.
            return h(AsyncHandler, data, children);
        },
    });
}
