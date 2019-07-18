<template>
    <div>
        <Loading :message="$t('jobs.results.alignment.loadingHits')"
                 v-if="loading"/>
        <div v-else>
            <div class="alignment-options d-flex align-items-center">
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

        <tool-citation-info :tool="tool"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
    import Loading from '@/components/utils/Loading.vue';
    import {Tool} from '@/types/toolkit/tools';
    import {Job} from '@/types/toolkit/jobs';
    import {resultsService} from '@/services/ResultsService';
    import {createTree, updateTree} from 'exelixis';
    import Logger from 'js-logger';

    const logger = Logger.get('TreeTab');

    export default Vue.extend({
        name: 'TreeTab',
        components: {
            ToolCitationInfo,
            Loading,
        },
        props: {
            job: {
                type: Object as () => Job,
                required: true,
            },
            tool: {
                type: Object as () => Tool,
                required: true,
            },
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
        mounted() {
            resultsService.getFile(this.job.jobID, this.filename)
                .then((data: string) => {
                    if (data) {
                        this.init(data);
                    }
                })
                .catch((e) => {
                    logger.error(e);
                });
            window.addEventListener('resize', this.updateTree);
        },
        beforeDestroy() {
            window.removeEventListener('resize', this.updateTree);
        },
        methods: {
            init(data: string): void {
                const opts = {
                    el: this.$refs.treeContainer,
                    tree: {
                        data,
                    },
                };
                this.tree = createTree(opts);
                this.updateTree();
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
                const downloadFilename = `${this.tool.longname}_${this.job.jobID}.tree`;
                resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
        },
    });
</script>

<style lang="scss" scoped>
    .alignment-options {
        font-size: 0.9em;

        a {
            cursor: pointer;
            margin-right: 3rem;
            color: inherit;
        }

        a:hover {
            color: $primary;
            text-decoration: none;
        }
    }
</style>

<style lang="scss">
    .tnt_groupDiv {
        margin-right: auto;
        margin-left: auto;
    }
</style>
