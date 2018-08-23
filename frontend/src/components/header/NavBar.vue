<template>
    <div id="navbar">
        <b-navbar toggleable="md" type="dark" variant="primary">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="(tool, group) in groups"
                                :key="group"
                                v-on:click="selectGroup(group)">
                        {{ group }}
                    </b-nav-item>
                </b-navbar-nav>
            </b-collapse>
        </b-navbar>

        <b-navbar toggleable="md" type="dark" variant="primary">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="tool in groups[selectedGroup]"
                                :key="tool.name"
                                v-bind:to="'/tools/' + tool.name">{{tool.nameLong}}
                    </b-nav-item>
                </b-navbar-nav>
            </b-collapse>
        </b-navbar>
    </div>
</template>

<script>
    import Vue from 'vue';

    export default Vue.extend({
        name: 'NavBar',
        data() {
            // @TODO: Extract tools into tool definition file (similar to /conf/tools.conf). Needs further planning.
            return {
                groups: {
                    Search: [
                        {
                            name: 'searchtool1',
                            nameLong: 'Search Tool 1',
                        },
                        {
                            name: 'searchtool2',
                            nameLong: 'Search Tool 2',
                        },
                    ],
                    Alignment: [
                        {
                            name: 'alignmenttool1',
                            nameLong: 'Alignment Tool 1',
                        },
                        {
                            name: 'alignmenttool2',
                            nameLong: 'Alignment Tool 2',
                        },
                    ],
                },
                selectedGroup: 'Search',
            };
        },
        methods: {
            selectGroup(group) {
                this.selectedGroup = group;
            },
        },
    });
</script>

<style>
</style>
