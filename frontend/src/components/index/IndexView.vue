<template>
    <div class="index-view">
        <div class="caption-container d-none d-sm-block">
            <img :src="require('../../assets/images/Toolkit100.png')"
                 class="img-fluid"/>
            <div class="caption d-none d-lg-block">
                <div class="caption-header">{{ $t('index.welcomeTitle') }}</div>
                <div class="caption-body">{{ $t('index.welcomeBody') }}</div>
            </div>
        </div>
        <ToolFinder/>
        <UpdatesSection/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ToolFinder from './ToolFinder.vue';
    import UpdatesSection from './UpdatesSection.vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'IndexView',
        mixins: [hasHTMLTitle],
        components: {
            ToolFinder,
            UpdatesSection,
        },
        watch: {
            // Use a watcher here - component cannot use 'beforeRouteEnter' because of lazy loading
            '$route.query': {
                immediate: true,
                async handler(query: any) {
                    if (query && query.action) {
                        EventBus.$emit('show-modal', {id: query.action});
                    }
                },
            },
        },
    });
</script>

<style lang="scss" scoped>
    .caption-container {
        position: relative;

        .img-fluid {
            border-radius: $global-radius;
        }
        .caption {
            border-top-right-radius: $global-radius;
            border-bottom-right-radius: $global-radius;
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
                font-size: 1.1em;
            }

            .caption-body {
                font-size: 0.9em;
                line-height: 1.5rem;
            }
        }
    }
</style>
