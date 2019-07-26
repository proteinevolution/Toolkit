<template>
    <div>
        <b-row class="mb-2">
            <b-col cols="12"
                   sm="5"
                   md="3"
                   lg="4"
                   xl="3">
                <label class="d-flex align-items-center">
                    <span v-text="$t('jobs.results.hitlist.table.perPage.show')"></span>
                    <b-form-select v-model="perPage" :options="perPageOptions"
                                   class="mx-2"></b-form-select>
                    <span v-text="$t('jobs.results.hitlist.table.perPage.entries')"></span>
                </label>
            </b-col>
            <b-col cols="12"
                   sm="7"
                   md="6"
                   offset-md="3"
                   offset-lg="2"
                   xl="4"
                   offset-xl="5">
                <label class="d-flex align-items-center justify-content-end">
                    <span v-text="$t('jobs.results.hitlist.table.filter')"></span>
                    <div class="ml-3 flex-grow-1">
                        <b-form-input v-model="filter"></b-form-input>
                    </div>
                </label>
            </b-col>
        </b-row>

        <b-table id="hitListTable"
                 :busy.sync="isBusy"
                 :items="hitsProvider"
                 :fields="fields"
                 :current-page="currentPage"
                 :per-page="perPage"
                 :filter="filter"
                 :empty-text="$t('jobs.results.hitlist.table.noData')"
                 :empty-filtered-text="$t('jobs.results.hitlist.table.noDataFiltered')"
                 responsive
                 striped
                 show-empty>
            <template #num="data">
                <a @click="$emit('elem-clicked', data.value)">{{ data.value }}</a>
            </template>
        </b-table>

        <div class="pagination-container"
             v-show="totalRows > perPage">
            <span v-text="$t('jobs.results.hitlist.table.paginationInfo', {start, end, total: totalRows})"
                  v-if="totalNoFilter === totalRows"></span>
            <span v-text="$t('jobs.results.hitlist.table.paginationInfoFiltered',
            {start, end, totalRows, totalNoFilter})"
                  v-else></span>
            <b-pagination
                    v-model="currentPage"
                    :total-rows="totalRows"
                    :per-page="perPage"
                    align="right"
                    class="mb-0"
                    aria-controls="hitListTable"
            ></b-pagination>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {resultsService} from '@/services/ResultsService';
    import {Job} from '@/types/toolkit/jobs';
    import {HHompHitsResponse} from '@/types/toolkit/results';

    export default Vue.extend({
        name: 'HitListTable',
        props: {
            fields: {
                type: Array as () => any[],
                required: true,
            },
            job: {
                type: Object as () => Job,
                required: true,
            },
        },
        data() {
            return {
                isBusy: false,
                totalRows: 1,
                totalNoFilter: 1,
                start: 0,
                end: 0,
                filter: '',
                currentPage: 1,
                perPage: 25,
                perPageOptions: [
                    10,
                    25,
                    50,
                    100,
                ],
            };
        },
        methods: {
            hitsProvider(ctx: any) {
                const start: number = (ctx.currentPage - 1) * ctx.perPage;
                const end: number = ctx.currentPage * ctx.perPage;
                return resultsService.fetchHits(this.job.jobID, start, end,
                    ctx.filter, ctx.sortBy, ctx.sortDesc)
                    .then((data: HHompHitsResponse) => {
                        const items = data.hits;
                        this.totalRows = data.total;
                        this.totalNoFilter = data.totalNoFilter;
                        this.start = data.start + 1;
                        this.end = Math.min(data.end + 1, data.total);
                        return items || [];
                    });
            },
        },
    });
</script>

<style lang="scss" scoped>
    #hitListTable {
        td {
            a {
                cursor: pointer;

                &:hover {
                    color: $primary;
                }
            }
        }
    }
</style>