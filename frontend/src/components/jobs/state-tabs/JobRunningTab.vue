<template>
    <div>
        <h3 class="mb-3 h5"
            v-text="$t('jobs.stateMessages.running')"></h3>
        <p v-html="$t('jobs.citationInfo', {tool: tool.longname})"></p>
        <p v-text="$t('jobs.jobIDDetails', job)"></p>
        <div class="mt-4">
            <div v-for="logElem in runningLog"
                 :key="logElem.text"
                 class="job-log-element mb-2">
                <i :class="logElem.class"
                   class="fas text-center"></i>
                <div class="ml-3">{{ logElem.text }}</div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

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
                runningLog: [],
            };
        },
        created() {
            (this.$options as any).sockets.onmessage = (response: any) => {
                const json = JSON.parse(response.data);
                if (json.mutation === 'SOCKET_WatchLogFile') {
                    if (json.jobID === this.job.jobID) {
                        this.runningLog = json.lines.split('#')
                            .filter((val: string) => val.trim() !== '')
                            .map((val: string) => {
                                const split = val.split('\n');
                                const res = {
                                    text: split[0],
                                    class: 'running',
                                };
                                if (split.length > 1 && split[1].trim() !== '') {
                                    res.class = split[1];
                                }
                                return res;
                            });
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
            height: 15px;
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
