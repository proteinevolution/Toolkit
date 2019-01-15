<template>
    <BaseModal :title="toolName"
               @close="$emit('close')">
        <b-tabs>
            <b-tab title="Overview" v-html="$t(`toolHelpModals.${toolName}.overview`)"></b-tab>
            <b-tab title="Input & Parameters">
                <Accordion :items="accordionItems"></Accordion>
            </b-tab>
            <b-tab title="References">
                <div v-html="$t(`citation`)"></div><br>
                <div v-html="$t(`toolHelpModals.${toolName}.references`)"></div>
            </b-tab>
            <b-tab title="Version" v-html="$t(`toolHelpModals.common.version`, ['TODO'])"></b-tab>
        </b-tabs>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import Accordion from '@/components/utils/Accordion.vue';
    import {AccordionItem} from '@/types/toolkit/utils';

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
            accordionItemsLength(): number {
                return (this.$t(`toolHelpModals.${this.toolName}.parameters`) as any).length;
            },
            accordionItems(): AccordionItem[] {
                // This is an i18n workaround, the linked locale messages (@:...) in the parameters array
                // are only resolved correctly if every message is translated individually (at least
                // according to my testing)
                return [...Array(this.accordionItemsLength).keys()].map((index) =>
                    ({
                        title: (this.$t(`toolHelpModals.${this.toolName}.parameters[${index}].title`) as string),
                        content: (this.$t(`toolHelpModals.${this.toolName}.parameters[${index}].content`) as string),
                    }));
            },
        },
    });
</script>

<style>
    .tab-pane {
        padding: 2em;
    }
</style>
