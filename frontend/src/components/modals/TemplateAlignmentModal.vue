<template>
    <BaseModal id="templateAlignmentModal" :title="$t('jobs.results.templateAlignment.title')" size="lmd">
        <b-form-select
            v-if="forwardingEnabled && !loading"
            v-model="selectedTool"
            :options="toolOptions"
            value-field="name"
            text-field="longname"
            class="select"
            :disabled="!data || loading"
            @change="forward">
            <template #first>
                <option :value="null" v-text="$t('jobs.results.templateAlignment.forwardTo')"></option>
            </template>
        </b-form-select>

        <Loading v-if="loading" :message="$t('loading')" class="float-right mb-3" :size="24" />

        <b-form-textarea :value="displayData" readonly class="file-view mb-2" />
    </BaseModal>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import BaseModal from './BaseModal.vue';
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import { resultsService } from '@/services/ResultsService';
import { ForwardingMode, Tool } from '@/types/toolkit/tools';
import { mapStores } from 'pinia';
import { useToolsStore } from '@/stores/tools';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import { useEventBus } from '@vueuse/core';

const logger = Logger.get('TemplateAlignmentModal');

export default defineComponent({
    name: 'TemplateAlignmentModal',
    components: {
        BaseModal,
        Loading,
    },
    props: {
        jobID: {
            type: String,
            required: true,
        },
        accession: {
            type: String,
            required: true,
        },
        forwardingMode: {
            type: Object as () => ForwardingMode,
            required: true,
        },
    },
    setup() {
        const { alert } = useToolkitNotifications();
        const forwardDataBus = useEventBus<{ data: string; jobID: string }>('forward-data');
        const hideModalsBus = useEventBus<string>('hide-modal');
        const pasteAreaLoadedBus = useEventBus<void>('paste-area-loaded');
        return { alert, forwardDataBus, hideModalsBus, pasteAreaLoadedBus };
    },
    data() {
        return {
            loading: true,
            data: '',
            selectedTool: null,
        };
    },
    computed: {
        tools(): Tool[] {
            return this.toolsStore.tools;
        },
        forwardingEnabled(): boolean {
            return Boolean(this.forwardingMode.templateAlignment);
        },
        toolOptions(): Tool[] {
            if (this.forwardingEnabled) {
                const options: string[] = this.forwardingMode.templateAlignment as string[];
                return this.tools
                    .filter((t: Tool) => options.includes(t.name))
                    .sort((t1: Tool, t2: Tool) => {
                        const t1Name = t1.longname.toLowerCase();
                        const t2Name = t2.longname.toLowerCase();
                        if (t1Name < t2Name) {
                            // sort string ascending
                            return -1;
                        } else if (t1Name > t2Name) {
                            return 1;
                        }
                        return 0;
                    });
            }
            return [];
        },
        displayData(): string {
            if (this.loading) {
                return '';
            } else if (!this.data) {
                return this.$t('errors.templateAlignmentFailed') as string;
            } else {
                return this.data;
            }
        },
        ...mapStores(useToolsStore),
    },
    watch: {
        jobID: {
            immediate: false,
            async handler(value: string) {
                if (value) {
                    this.loadData();
                }
            },
        },
        accession: {
            immediate: false,
            async handler(value: string) {
                if (value) {
                    this.loadData();
                }
            },
        },
    },
    methods: {
        async loadData() {
            this.data = '';
            this.loading = true;
            await resultsService.generateTemplateAlignment(this.jobID, this.accession);
            this.data = await resultsService.getFile(this.jobID, this.accession);
            if (!this.data) {
                this.alert(this.$t('errors.templateAlignmentFailed'), 'danger');
            }
            this.loading = false;
        },
        forward() {
            if (this.selectedTool) {
                this.$router.push('/tools/' + this.selectedTool, () => {
                    this.pasteAreaLoadedBus.on(this.pasteForwardData);
                });
                this.hideModalsBus.emit('templateAlignmentModal');
                this.resetData();
            } else {
                logger.log('no tool selected');
            }
        },
        pasteForwardData() {
            this.pasteAreaLoadedBus.off(this.pasteForwardData);
            this.forwardDataBus.emit({ data: this.data, jobID: this.jobID });
        },
        resetData() {
            this.selectedTool = null;
        },
    },
});
</script>

<style lang="scss" scoped>
.file-view {
    background: white !important;
    font-size: 0.7rem;
    height: 50vh;
    font-family: $font-family-monospace;
}

.select {
    width: 12em;
    margin-bottom: 1em;
}

.vue-simple-spinner {
    margin-left: 20rem !important;
}
</style>
