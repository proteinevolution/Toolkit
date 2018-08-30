<template>
    <footer>
        <b-nav class="modals-nav">
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.help') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.faq') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.privacy') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.imprint') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.contact') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.cite') }}</b-nav-item>
            <b-nav-item @click="launchHelpModal">{{ $t('helpModals.names.updates') }}</b-nav-item>
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
    import Help from './modals/Help.vue';

    export default Vue.extend({
        name: 'Footer',
        methods: {
            launchHelpModal() {
                this.$modal.show(Help, {}, {
                    draggable: false,
                    width: '60%',
                    height: 'auto',
                    scrollable: true,
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
        font-size: 0.75em;
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
