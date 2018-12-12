<template>
    <b-tabs>
        <b-tab title="Overview" v-html="$t('toolHelpModals.hhblits.overview')"></b-tab>
        <b-tab title="Input & Parameters">
            <Accordion :items="accordionItems"></Accordion>
        </b-tab>
        <b-tab title="References" v-html="$t('toolHelpModals.hhblits.references')"></b-tab>
        <b-tab title="Version" v-html="$t('toolHelpModals.hhblits.version', ['TODO'])"></b-tab>
    </b-tabs>
</template>

<script>
    // TODO move this in HelpModal, but take care of lazy loading the translations

    import Accordion from '@/components/utils/Accordion.vue';

    export default {
        name: 'hhblitsHelp',
        data() {
            return {
                accordionItems: this.$t('toolHelpModals.hhblits.parameters'),
            };
        },
        components: {
            Accordion,
        },
    };
</script>
