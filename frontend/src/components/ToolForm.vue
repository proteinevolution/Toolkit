<template>
    <div id="Tool Form">
        Tool Form view for {{ id }}
    </div>
</template>

<script>
    import Vue from 'vue';

    export default Vue.extend({
        name: 'toolform',
        props: {
            id: String,
        },
    });
</script>