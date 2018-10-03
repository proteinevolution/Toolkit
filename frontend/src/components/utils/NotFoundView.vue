<template>
    <div class="page-not-found">
        <h5 class="text-center text-muted">{{ $t(errorMessage) }}</h5>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '../../mixins/hasHTMLTitle';

    export default Vue.extend({
        name: 'NotFoundView',
        mixins: [hasHTMLTitle],
        props: {
            errorMessage: {
                type: String,
                required: false,
                default: 'errors.PageNotFound',
            },
        },
        computed: {
            htmlTitle() {
                return '404';
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
