<template>
    <div id="navbar">
        <b-navbar  toggleable="md" type="dark" variant="primary">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="(tools, group) in groups" v-on:click="selectGroup(group)">{{ group }}</b-nav-item>
                </b-navbar-nav>
            </b-collapse>
        </b-navbar>

        <b-navbar  toggleable="md" type="dark" variant="primary">
            <b-navbar-toggle target="nav_collapse"></b-navbar-toggle>

            <b-collapse is-nav id="nav_collapse">
                <b-navbar-nav>
                    <b-nav-item v-for="tool in groups[selectedGroup]" v-bind:to="'/tools/' + tool[1]">{{tool[0]}}</b-nav-item>
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
            return {
                groups: {
                    'Search': [
                        ['Search Tool1', 'searchtool1' ],
                        ['Search Tool2', 'searchtool2' ],
                    ],
                    'Alignment': [
                        ['Alignment Tool1', 'alignmenttool1'],
                        ['Alignment Tool2', 'alignmenttool2'],
                    ],
                },
                selectedGroup: 'Search',
            }
        },
        methods: {
            selectGroup(group) {
                this.selectedGroup = group;
            }
        }
    });
</script>

<style>
</style>