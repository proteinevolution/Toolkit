<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options">
            <a @click="forwardToModeller">{{$t('jobs.results.actions.forwardToModeller')}}</a>
        </div>

        <pre v-html="file"
             class="file-view"></pre>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('TemplateSelectionViewTab');

    export default mixins(ResultTabMixin).extend({
        name: 'TemplateSelectionViewTab',
        components: {
            Loading,
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
                    EventBus.$on('paste-area-loaded', this.pasteForwardData);
                });
            },
            pasteForwardData(): void {
                EventBus.$off('paste-area-loaded', this.pasteForwardData);
                EventBus.$emit('forward-data', {data: this.file, jobID: this.job.jobID});
            },
        },
    });
</script>

<style lang="scss" scoped>
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
