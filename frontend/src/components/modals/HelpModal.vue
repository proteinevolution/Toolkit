<template>
    <BaseModal :title="toolName"
               @close="$emit('close')">
        <b-tabs>
            <b-tab title="Overview" v-html="$t(`toolHelpModals.${toolName}.overview`)"></b-tab>
            <b-tab title="Input & Parameters">
                <Accordion :items="accordionItems"></Accordion>
            </b-tab>
            <b-tab title="References" v-html="$t(`toolHelpModals.${toolName}.references`)"></b-tab>
            <b-tab title="Version" v-html="$t(`toolHelpModals.${toolName}.version`, ['TODO'])"></b-tab>
        </b-tabs>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import Accordion from '@/components/utils/Accordion.vue';

    export default Vue.extend({
        name: 'HelpModal',
        components: {
            BaseModal,
            Accordion,
        },
        props: {
            toolName: {
                type: String,
                required: true,
            },
        },
        computed: {
            accordionItems(): Array<[string, string]> {
                return (this.$t(`toolHelpModals.${this.toolName}.parameters`) as any);
            },
        },
    });
</script>

<style>
    .tab-pane {
        padding: 2em;
    }
</style>
