<template>
    <div class="home">
        {{ $t("welcome") }}
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'home',
    });
</script>
