<template>
    <div class="tool-finder-container">
        <div class="tool-finder container">
            <div class="live-table row">
                <div class="col-md-6">
                    <div class="live-table-child">
                        <div class="load-bar">
                            <div class="load-bar-label">
                                {{ $t('index.loadBarLabel', {load: clusterWorkload}) }}
                            </div>
                            <div class="load-bar-graph">
                                <div class="load-bar-segment"
                                     :class="[clusterWorkload > 0 ? 'green' : '']"></div>
                                <div class="load-bar-segment"
                                     :class="[clusterWorkload > 25 ? 'green' : '']"></div>
                                <div class="load-bar-segment"
                                     :class="[clusterWorkload > 50 ? 'green' : '']"></div>
                                <div class="load-bar-segment"
                                     :class="[clusterWorkload > 75 ? 'green' : '']"></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="live-table-child">
                        <router-link to="/jobmanager"
                                     class="job-manager-link">
                            Job Manager
                            <i class="fas fa-list-ul"></i>
                        </router-link>
                    </div>
                </div>
            </div>
            <div class="search-container row">
                <div class="traffic-bar col-12"
                     :class="currentJobStatus"></div>
                <div class="search-field-container col-12">
                    <SearchField :placeholder="$t('index.searchPlaceholder')" />
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import SearchField from './SearchField.vue';

    export default Vue.extend({
        name: 'ToolFinder',
        data() {
            return {
                clusterWorkload: 0,
            };
        },
        components: {
            SearchField,
        },
        computed: {
            currentJobStatus(): string {
                return 'done'; // TODO: compute correct state
            },
            storeClusterWorkload(): number {
                return this.$store.state.clusterWorkload;
            },
        },
        watch: {
            storeClusterWorkload: {
                immediate: true,
                handler(value: number) {
                    if (value !== this.clusterWorkload) {
                        this.animateClusterload(value);
                    }
                },
            },
        },
        methods: {
            animateClusterload(end: number, duration: number = 1000): void {
                const start: number = this.clusterWorkload;
                const range: number = end - start;
                const increment: number = end > start ? 1 : -1;
                const stepTime: number = Math.abs(Math.floor(duration / range));
                const timer = setInterval(() => {
                    this.clusterWorkload += increment;
                    if (this.clusterWorkload === end) {
                        clearInterval(timer);
                    }
                }, stepTime);
            },
        },
    });
</script>

<style lang="scss" scoped>
    @keyframes pulse {
        0% {
            background: rgba(255, 255, 255, 1);
        }
        100% {
            background: rgba(255, 255, 255, 0);
        }
    }

    .tool-finder-container {
        width: 50%;
        margin: -83px auto 0 auto;

        .tool-finder {
            position: relative;
            width: 100%;
            height: 10rem;
            box-shadow: 0 1px 2px 0 $tk-light-gray;
            border-radius: 3px;
            border: 1px solid $tk-light-gray;

            .live-table {
                background: rgba(214, 214, 214, 0.96);
                display: flex;
                height: 50%;
                flex-grow: 1;

                .live-table-child {
                    display: flex;
                    align-items: center;
                    height: 100%;

                    .load-bar {
                        display: flex;
                        align-items: center;
                        flex-direction: column;
                        width: 100%;

                        .load-bar-label {
                            font-size: 0.75em;
                            color: $tk-gray;
                            margin-bottom: 4px;
                        }

                        .load-bar-graph {
                            display: flex;
                            flex-direction: row;
                            flex-grow: 1;

                            .load-bar-segment {
                                border-radius: 3px;
                                margin: 0 1px;
                                padding: 1px;
                                width: 2rem;
                                height: 0.6rem;
                                box-shadow: inset 0 0 10px 2px rgba(117, 182, 255, 0.4), 0 0 4px rgba(117, 182, 255, 0.1);
                                transition: background-color 2s linear;

                                &.green {
                                    animation: pulse 1s alternate infinite;
                                    background-color: rgba(255, 255, 255, 0.9);
                                    box-shadow: inset 0 0 10px 2px rgba(0, 180, 40, 0.83), 0 0 4px rgba(143, 243, 0, 0.49);
                                }
                            }
                        }
                    }

                    .job-manager-link {
                        font-weight: bold;
                        font-size: 0.9em;
                        width: 100%;
                        text-align: center;

                        &:hover, &:active {
                            text-decoration: none;
                        }

                        i {
                            margin-left: 1.5rem;
                        }
                    }
                }
            }

            .search-container {
                height: 50%;

                .traffic-bar {
                    cursor: pointer;
                    z-index: 99;
                    height: 0.2em;
                    border-radius: 3px;
                    animation: background 10s cubic-bezier(1, 0, 0, 1) infinite;
                    box-shadow: -2px 0 4px $tk-lighter-gray;

                    &.queue {
                        background: rgba(192, 181, 191, 1);
                        height: 0.3em;
                    }

                    &.done {
                        background: rgba(0, 180, 40, 0.9);
                    }

                    &.error {
                        background: rgba(180, 0, 40, 0.9);
                    }

                    &.running {
                        background: rgba(255, 255, 0, 0.9);
                    }

                    &.not_init {
                        display: none;
                    }
                }

                .search-field-container {
                    display: flex;
                    align-items: center;
                    height: 100%;
                    padding: 0 1.1rem;
                }
            }
        }
    }
</style>
