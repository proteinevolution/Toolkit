<template>
    <b-container>
        <b-row>
            <b-col>
                <span class="page-not-found">Page not found.</span>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';

    export default Vue.extend({
        name: 'NotFoundView',
        mixins: [hasHTMLTitle],
        computed: {
            htmlTitle() {
                return '404';
            },
        },
    });
</script>

<style scoped>
    .page-not-found {
        text-align: center;
    }
</style>
