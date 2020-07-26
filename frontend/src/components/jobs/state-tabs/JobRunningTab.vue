<template>
    <div>
        <h3 class="mb-3 h5"
            v-text="$t('jobs.stateMessages.running')"></h3>
        <p v-html="$t('jobs.citationInfo', {tool: tool.longname})"></p>
        <p class="mb-3"
           v-text="$t('jobs.jobIDDetails', job)"></p>
        <table v-for="logElem in runningLog"
               :key="logElem.text"
               class="job-log-element mb-2">
            <tr>
                <td>
                    <i :class="logElem.class"
                       class="fas text-center mr-1"></i>
                </td>
                <td>{{ logElem.text }}</td>
            </tr>
        </table>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {parseProcessLog} from '@/util/Utils';
    import {ProcessLogItem} from '@/types/toolkit/jobs';

    export default Vue.extend({
        name: 'JobRunningTab',
        props: {
            job: {
                type: Object,
                required: true,
            },
            tool: {
                type: Object,
                required: true,
            },
        },
        data() {
            return {
                runningLog: [] as ProcessLogItem[],
            };
        },
        created() {
            (this.$options as any).sockets.onmessage = (response: any) => {
                const json = JSON.parse(response.data);
                if (json.mutation === 'SOCKET_WatchLogFile') {
                    if (json.jobID === this.job.jobID) {
                        this.runningLog = parseProcessLog(json.lines);
                    }
                }
            };
        },
        destroyed(): void {
            delete (this.$options as any).sockets.onmessage;
        },
    });
</script>

<style lang="scss" scoped>
    .job-log-element {
        display: flex;
        align-items: center;

        i {
            width: 32px;
            height: 18px;
            margin-top: 0.2em;
            font-size: 1.3em;
        }

        .running {
            background: url('../../../assets/images/ellipsis.gif') no-repeat center center;
        }

        .done {
            color: rgba(40, 120, 111, 0.636);

            &:before {
                content: '\f058';
            }
        }

        .error {
            color: #da4453;

            &:before {
                content: '\f057';
            }
        }
    }
</style>
