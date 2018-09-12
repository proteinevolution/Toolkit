<template>
    <div class="job-list">
        <div class="job-list-controls">
            <div v-for="sortCol in sortColumns"
                 :key="sortCol"
                 class="sort"
                 @click="selectedSort = sortCol"
                 :class="[selectedSort === sortCol ? 'selected':'']">
                {{ $t('jobList.sortColumns.' + sortCol) }}
            </div>
            <div class="open-job-manager">
                <router-link to="/jobmanager">
                    <i class="fas fa-list-ul"></i>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'JobList',
        data() {
            return {
                sortColumns: ['id', 'date', 'tool'],
                selectedSort: 'date',
            };
        },
    });
</script>

<style lang="scss" scoped>
    .job-list {
        margin-top: 1rem;

        .job-list-controls {
            display: flex;
            justify-content: space-between;
            text-align: center;
            width: 100%;
            margin-bottom: 1rem;
            box-shadow: 1px 1px 2px 1px $tk-light-gray;
            background-color: transparent;
            cursor: pointer;
            border: 1 solid rgba($black, .125);
            border-radius: $global-radius;

            .sort {
                font-weight: bold;
                color: $tk-dark-gray;
                font-size: 0.7em;
                padding: 0.4rem 6%;

                &.selected {
                    color: $primary;
                }
            }

            .open-job-manager {
                padding: 0.3rem 0.7rem;
                font-size: 0.9em;
                color: $primary;
            }
        }
    }
</style>
