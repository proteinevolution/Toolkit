import HomeView from '../views/HomeView.vue';

// route level code-splitting
// this generates a separate chunk (about.[hash].js) for this route
// which is lazy-loaded when the route is visited.
const ToolView = () => import(/* webpackChunkName: "about" */ '../views/ToolView.vue');
const JobManagerView = () => import(/* webpackChunkName: "about" */ '../views/JobManagerView.vue');

export default [
    {
        path: '/',
        component: HomeView,
    },
    {
        path: '/tools/:id',
        component: ToolView,
    },
    {
        path: '/jobmanager',
        component: JobManagerView,
    },
];
