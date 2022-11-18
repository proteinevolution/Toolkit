<template>
    <footer>
        <b-nav class="modals-nav justify-content-center">
            <b-nav-item v-for="simpleModal in simpleModals" :key="simpleModal" @click="launchHelpModal(simpleModal)">
                {{ $t('footerLinkModals.names.' + simpleModal) }}
            </b-nav-item>
            <b-nav-item @click="launchUpdatesModal">
                {{ $t('footerLinkModals.names.updates') }}
            </b-nav-item>
        </b-nav>
        <b-row>
            <b-col class="text-center" cols="auto">
                {{ $t('copyright', { currentYear: new Date().getFullYear() }) }}
            </b-col>
        </b-row>
    </footer>
</template>

<script setup lang="ts">
import { ModalParams } from '@/types/toolkit/utils';
import { useEventBus } from '@vueuse/core';

const simpleModals = ['help', 'faq', 'privacy', 'imprint', 'contact', 'cite'];

const showModalsBus = useEventBus<ModalParams>('show-modal');

function launchHelpModal(modal: string): void {
    showModalsBus.emit({ id: 'footerLink', props: { modal } });
}

function launchUpdatesModal(): void {
    showModalsBus.emit({ id: 'updates' });
}
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

        .nav-item a:hover {
            color: $primary;
        }
    }
}
</style>
