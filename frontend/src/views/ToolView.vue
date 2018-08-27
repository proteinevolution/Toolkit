<template>
    <b-container>
        <b-row>
            <b-col>
                <Jobs></Jobs>
            </b-col>
            <b-col>
                <ToolForm v-bind:id="$route.params.toolName"></ToolForm>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Jobs from '@/components/Jobs.vue';
    import ToolForm from '@/components/ToolForm.vue';

    export default Vue.extend({
        name: 'ToolView',
        components: {
            Jobs,
            ToolForm,
        },
        beforeRouteEnter(to, from, next) {
            next(vm => {
                if (!vm.$store.getters['tools/tools'].some(tool => tool.name === to.params.toolName)) {
                    next({path: '/404',})
                } else {
                    next()
                }
            })
        }
    });
</script>
