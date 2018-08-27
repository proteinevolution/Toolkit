import HomeView from '../views/HomeView.vue';

// route level code-splitting
// this generates a separate chunk (about.[hash].js) for this route
// which is lazy-loaded when the route is visited.
const ToolView = () => import(/* webpackChunkName: "about" */ '../views/ToolView.vue');
const JobManagerView = () => import(/* webpackChunkName: "about" */ '../views/JobManagerView.vue');
const NotFoundView = () => import(/* webpackChunkName: "about" */ '../views/NotFoundView.vue');

export default [
    {
        path: '/',
        component: HomeView,
    },
    {
        path: '/tools/:toolName',
        component: ToolView,
    },
    {
        path: '/jobmanager',
        component: JobManagerView,
    },
    {
        path: '/404',
        component: NotFoundView,
    },
    {
        path: '/**',
        redirect: '/404',
    }
];
