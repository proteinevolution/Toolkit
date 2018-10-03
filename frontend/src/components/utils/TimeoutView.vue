<template>
    <NotFoundView :errorMessage="'Connection Timeout!'"></NotFoundView>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '../../mixins/hasHTMLTitle';
    import NotFoundView from '@/components/utils/NotFoundView.vue';

    export default Vue.extend({
        name: 'TimeoutView',
        mixins: [hasHTMLTitle],
        components: {
            NotFoundView,
        },
        computed: {
            htmlTitle() {
                return 'Timeout';
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
