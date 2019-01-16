<template>
    <BaseModal :title="tool.longname"
               id="helpModal">
        <b-tabs v-if="toolName">
            <b-tab class="helpTab"
                   title="Overview"
                   v-html="$t(`toolHelpModals.${toolName}.overview`)">
            </b-tab>
            <b-tab class="helpTab"
                   title="Input & Parameters">
                <Accordion :items="accordionItems"></Accordion>
            </b-tab>
            <b-tab class="helpTab"
                   title="References">
                <div v-html="$t(`citation`)"></div><br>
                <div v-html="$t(`toolHelpModals.${toolName}.references`)"></div>
            </b-tab>
            <b-tab v-if="tool.version"
                   class="helpTab"
                   title="Version"
                   v-html="$t(`toolHelpModals.common.version`, [tool.version])">
            </b-tab>
        </b-tabs>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import Accordion from '@/components/utils/Accordion.vue';
    import {AccordionItem} from '@/types/toolkit/utils';
    import {Tool} from '@/types/toolkit/tools';

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
            tool(): Tool {
                return this.$store.getters['tools/tools'].find((tool: Tool) => tool.name === this.toolName);
            },
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

<style lang="scss" scoped>
    .helpTab {
        padding-top: 1rem;
    }
</style>
