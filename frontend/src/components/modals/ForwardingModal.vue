<template>
    <BaseModal :title="$t('jobs.results.actions.forward')"
               id="forwardingModal"
               size="md">
        <b-form-select v-model="selectedTool"
                       :options="toolOptions"
                       value-field="name"
                       text-field="longname">
            <template slot="first">
                <option :value="null"
                        v-text="$t('jobs.forwarding.selectPlaceholder')"></option>
            </template>
        </b-form-select>
        <b-button variant="primary"
                  v-text="$t('jobs.results.actions.forward')"
                  @click="forward"
                  class="mt-3"
                  :disabled="!selectedTool"></b-button>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {ForwardingMode, Tool} from '@/types/toolkit/tools';
    import EventBus from '@/util/EventBus';
    import Logger from 'js-logger';

    const logger = Logger.get('ForwardingModal');

    export default Vue.extend({
        name: 'ForwardingModal',
        components: {
            BaseModal,
        },
        props: {
            forwardingMode: {
                type: Object as () => ForwardingMode,
                required: true,
            },
            forwardingData: {
                type: String,
                required: true,
            },
            forwardingJobID: {
                type: String,
                required: true,
            },
        },
        data() {
            return {
                selectedTool: null,
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            toolOptions(): Tool[] {
                const alignmentOptions: string[] = (this.forwardingMode as ForwardingMode).alignment;
                if (alignmentOptions) {
                    return this.tools.filter((t: Tool) => alignmentOptions.includes(t.name));
                } else {
                    return [];
                }
            },
        },
        methods: {
            forward() {
                if (this.selectedTool) {
                    this.$router.push('/tools/' + this.selectedTool, () => {
                        EventBus.$on('paste-area-loaded', this.pasteForwardData);
                    });
                    EventBus.$emit('hide-modal', 'forwardingModal');
                    this.resetData();
                } else {
                    logger.log('no tool selected');
                }
            },
            pasteForwardData() {
                EventBus.$off('paste-area-loaded', this.pasteForwardData);
                EventBus.$emit('forward-data', {data: this.forwardingData, jobID: this.forwardingJobID});
            },
            resetData() {
                // reset data
                this.selectedTool = undefined;
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
