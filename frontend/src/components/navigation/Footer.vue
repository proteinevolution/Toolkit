<template>
    <footer>
        <b-nav class="modals-nav justify-content-center">
            <b-nav-item v-for="simpleModal in simpleModals"
                        :key="simpleModal"
                        @click="launchHelpModal(simpleModal)">
                {{ $t('footerLinkModals.names.' + simpleModal) }}
            </b-nav-item>
            <b-nav-item @click="launchUpdatesModal">{{ $t('footerLinkModals.names.updates') }}</b-nav-item>
        </b-nav>
        <b-row>
            <b-col class="text-center"
                   cols="12">
                {{ $t('copyright', {currentYear: new Date().getFullYear()}) }}
            </b-col>
        </b-row>
    </footer>
</template>

<script lang="ts">
    import Vue from 'vue';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'Footer',
        data() {
            return {
                simpleModals: [
                    'help',
                    'faq',
                    'privacy',
                    'imprint',
                    'contact',
                    'cite',
                ],
            };
        },
        methods: {
            launchHelpModal(modal: string): void {
                EventBus.$emit('show-modal', {id: 'footerLink', props: {modal}});
            },
            launchUpdatesModal(): void {
                EventBus.$emit('show-modal', {id: 'updates'});
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
