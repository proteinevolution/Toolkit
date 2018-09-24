import IndexView from '../components/index/IndexView.vue';

// route level code-splitting
// this generates a separate chunk (about.[hash].js) for this route
// which is lazy-loaded when the route is visited.
const ToolView = () => import(/* webpackChunkName: "tool" */ '../components/tools/ToolView.vue');
const JobManagerView = () => import(/* webpackChunkName: "jobmanager" */ '../components/jobmanager/JobManagerView.vue');
const NotFoundView = () => import(/* webpackChunkName: "404" */ '../components/NotFoundView.vue');

export default [
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
        path: '/jobmanager',
        name: 'jobmanager',
        component: JobManagerView,
        meta: {
            showJobList: true,
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
