<template>
    <NotFoundView :error-message="'Connection Timeout!'" />
</template>

<script lang="ts">
import hasHTMLTitle from '../../mixins/hasHTMLTitle';
import NotFoundView from '@/components/utils/NotFoundView.vue';

export default hasHTMLTitle.extend({
    name: 'TimeoutView',
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
