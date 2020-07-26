<template>
    <NotFoundView :error-message="'Connection Timeout!'"/>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '../../mixins/hasHTMLTitle';
    import NotFoundView from '@/components/utils/NotFoundView.vue';

    export default Vue.extend({
        name: 'TimeoutView',
        components: {
            NotFoundView,
        },
        mixins: [hasHTMLTitle],
        computed: {
            htmlTitle() {
                return 'Timeout';
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
