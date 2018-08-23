<template>
    <div class="tool-form">
        Tool Form view for {{ id }}
    </div>
</template>

<script>
    import Vue from 'vue';

    export default Vue.extend({
        name: 'ToolForm',
        props: {
            id: String,
        },
    });
</script>
