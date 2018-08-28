<template>
    <div class="index-view">
        <div class="caption-container">
            <img :src="require('../../assets/images/Toolkit100.png')"
                 class="img-fluid"/>
            <div class="caption">
                <div class="caption-header">{{ $t('index.welcomeTitle') }}</div>
                <div class="caption-body">{{ $t('index.welcomeBody') }}</div>
            </div>
        </div>
        <ToolFinder></ToolFinder>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ToolFinder from './ToolFinder.vue';

    export default Vue.extend({
        name: 'IndexView',
        components: {ToolFinder}
    });
</script>

<style lang="scss" scoped>
    .caption-container {
        position: relative;

        .caption {
            position: absolute;
            height: 100%;
            width: 23%;
            color: white;
            letter-spacing: 1px;
            background: rgba(4, 4, 4, 0.9);
            opacity: 0.55;
            top: 0;
            right: 0;
            padding: 2.5rem 2rem;

            .caption-header {
                margin-bottom: 1rem;
            }

            .caption-body {
                font-size: 0.8em;
                line-height: 1.5rem;
            }
        }
    }
</style>