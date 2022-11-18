<template>
    <Loading v-if="loading" :message="$t('loading')" />
    <div v-else>
        <pre class="file-view" v-html="file"></pre>
        <div class="result-options">
            <b-btn type="button" variant="primary" class="submit-button float-right" @click="forwardToModeller">
                {{ $t('jobs.results.actions.forwardToModeller') }}
            </b-btn>
        </div>
    </div>
</template>

<script lang="ts">
import ResultTabMixin from '@/mixins/ResultTabMixin';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import { useEventBus } from '@vueuse/core';

export default ResultTabMixin.extend({
    name: 'TemplateSelectionViewTab',
    components: {
        Loading,
    },
    setup() {
        const forwardDataBus = useEventBus<{ data: string; jobID: string }>('forward-data');
        const pasteAreaLoadedBus = useEventBus<void>('paste-area-loaded');
        return { forwardDataBus, pasteAreaLoadedBus };
    },
    data() {
        return {
            file: '',
        };
    },
    computed: {
        filename(): string {
            return 'tomodel.pir';
        },
    },
    methods: {
        async init() {
            this.file = await resultsService.getFile(this.job.jobID, this.filename);
        },
        forwardToModeller(): void {
            this.$router.push('/tools/modeller', () => {
                this.pasteAreaLoadedBus.on(this.pasteForwardData);
            });
        },
        pasteForwardData(): void {
            this.pasteAreaLoadedBus.off(this.pasteForwardData);
            this.forwardDataBus.emit({ data: this.file, jobID: this.job.jobID });
        },
    },
});
</script>

<style lang="scss" scoped>
.result-options {
    border-bottom: none;
    border-top: 1px solid rgba(10, 10, 10, 0.1);
}

.file-view {
    width: 100%;
    font-size: 12px;
    height: 50vh;
    font-family: $font-family-monospace;
}

.fullscreen .file-view {
    height: 85vh;
}
</style>
