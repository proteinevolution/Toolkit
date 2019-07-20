<template>
    <span>Hitlist</span>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';

    export default mixins(ResultTabMixin).extend({
        name: 'HitlistTab',
    });
</script>

<style lang="scss" scoped>

</style>
