import IndexView from '../components/index/IndexView.vue';

// route level code-splitting
// this generates a separate chunk (about.[hash].js) for this route
// which is lazy-loaded when the route is visited.
const ToolView = () => import(/* webpackChunkName: "about" */ '../views/ToolView.vue');
const JobManagerView = () => import(/* webpackChunkName: "about" */ '../components/jobmanager/JobManagerView.vue');
const NotFoundView = () => import(/* webpackChunkName: "about" */ '../components/NotFoundView.vue');

export default [
    {
        path: '/',
        component: IndexView,
        meta: {
            showJobList: false,
        },
    },
    {
        path: '/tools/:toolName',
        component: ToolView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/jobmanager',
        component: JobManagerView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/404',
        component: NotFoundView,
        meta: {
            showJobList: true,
        },
    },
    {
        path: '/**',
        redirect: '/404',
    },
];
