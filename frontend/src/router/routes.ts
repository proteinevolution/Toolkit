import Home from '../views/Home.vue';

// route level code-splitting
// this generates a separate chunk (about.[hash].js) for this route
// which is lazy-loaded when the route is visited.
const Tool = () => import(/* webpackChunkName: "about" */ '../views/Tool.vue');

export default [
    {
        path: '/',
        component: Home,
    },
    {
        path: '/tools/:id',
        component: Tool,
    },
];
