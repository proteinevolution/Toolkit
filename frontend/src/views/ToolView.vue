<template>
    <b-container>
        <b-row>
            <b-col>
                <Jobs></Jobs>
            </b-col>
            <b-col>
                <ToolForm v-bind:id="$route.params.id"></ToolForm>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from "vue";
    import Jobs from "@/components/Jobs.vue";
    import ToolForm from "@/components/ToolForm.vue";

    export default Vue.extend({
        name: "ToolView",
        components: {
            Jobs,
            ToolForm,
        },
    });
</script>
