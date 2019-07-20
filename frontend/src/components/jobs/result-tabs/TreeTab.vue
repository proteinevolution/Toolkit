<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options d-flex align-items-center">
            <b-form-select @input="updateTree"
                           v-model="treeOpts.tree.layoutInput"
                           :options="layoutOptions"
                           size="sm"
                           class="w-auto"/>
            <a @click="download"
               class="ml-auto">{{$t('jobs.results.actions.downloadTree')}}</a>
        </div>
        <hr class="mt-2">

        <div ref="treeContainer"></div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import {createTree, updateTree} from 'exelixis';
    import Logger from 'js-logger';

    const logger = Logger.get('TreeTab');

    export default mixins(ResultTabMixin).extend({
        name: 'TreeTab',
        components: {
            Loading,
        },
        data() {
            return {
                tree: undefined as any,
                treeOpts: {
                    tree: {
                        width: 600,
                        heigth: 20,
                        layoutInput: 'radial',
                    },
                    nodes: {
                        size: 5,
                        fill: '#2E8C81',
                        stroke: 'black',
                    },
                },
                layoutOptions: [
                    {value: 'radial', text: 'Radial'},
                    {value: 'vertical', text: 'Vertical'},
                ],
                loading: false,
            };
        },
        computed: {
            downloadFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, this.filename);
            },
            filename(): string {
                return `${this.job.jobID}.tree`;
            },
        },
        beforeDestroy() {
            window.removeEventListener('resize', this.updateTree);
        },
        methods: {
            async init() {
                const data: string = await resultsService.getFile(this.job.jobID, this.filename);
                const opts = {
                    el: this.$refs.treeContainer,
                    tree: {
                        data,
                    },
                };
                this.tree = createTree(opts);
                this.updateTree();
                window.addEventListener('resize', this.updateTree);
            },
            updateTree(): void {
                if (this.tree) {
                    this.treeOpts.tree.width = (this.$refs.treeContainer as HTMLElement).clientWidth * 0.75;
                    updateTree(this.tree, this.treeOpts);
                    this.tree.on_click((node: any) => {
                        node.toggle();
                        this.tree.update();
                    });
                }
            },
            download(): void {
                const downloadFilename = `${this.tool.name}_${this.job.jobID}.tree`;
                resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>

<style lang="scss">
    .tnt_groupDiv {
        margin-right: auto;
        margin-left: auto;
    }
</style>
