<template>
    <div>
        <hr>
        <span v-text="$t('tools.citations.intro', {tool: tool.longname})"></span>
        <div class="citation">
            <div v-html="$t('citation')"></div>
            <br>
            <div v-html="$t('tools.citations.' + tool.name)"></div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Tool} from '@/types/toolkit/tools';

    export default Vue.extend({
        name: 'ToolCitationInfo',
        props: {
            tool: {
                type: Object as () => Tool,
                required: true,
            },
        },
    });
</script>

<style lang="scss" scoped>
    .citation {
        border-left: 2px solid rgba(0, 110, 98, 0.75);
        margin: 0.5em 0.5em 0;

        div {
            margin-left: 1em;
        }
    }
</style>
