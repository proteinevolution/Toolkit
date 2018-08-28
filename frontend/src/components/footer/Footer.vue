<template>
    <footer>
        <b-nav class="modals-nav">
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.help') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.faq') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.privacy') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.imprint') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.contact') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.cite') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.titles.updates') }}</b-nav-item>
        </b-nav>
        <b-row>
            <b-col>
                {{ $t('copyright', {currentYear: new Date().getFullYear()}) }}
            </b-col>
        </b-row>
    </footer>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Help from '@/components/footer/Help.vue';

    export default Vue.extend({
        name: 'Footer',
        methods: {
            launchHelpModal() {
                this.$modal.show(Help, {}, {
                    draggable: false,
                });
            },
        },
    });
</script>

<style lang="scss" scoped>
    footer {
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        font-size: 0.7em;
        padding: 1rem;
        color: $tk-gray;

        .modals-nav {
            margin-bottom: 0.5rem;
            .nav-item a {
                color: $tk-gray;
            }
        }
    }
</style>
