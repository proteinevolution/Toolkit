<template>
    <input class="form-control search-field"
           v-bind="$attrs"
           v-on="listeners"/>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'SearchField',
        inheritAttrs: false,
        computed: {
            listeners() {
                return {
                    ...this.$listeners,
                    input: event =>
                        this.$emit('input', event.target.value),
                };
            },
        },
    });
</script>

<style lang="scss" scoped>
    .search-field {
        font-size: 0.9em;
    }
</style>
