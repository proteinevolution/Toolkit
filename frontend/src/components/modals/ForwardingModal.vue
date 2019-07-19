<template>
    <BaseModal :title="$t('jobs.results.actions.forward')"
               id="forwardingModal"
               size="md">
        <b-form-select v-model="selectedTool"
                       :options="toolOptions"
                       value-field="name"
                       text-field="longname">
            <template slot="first">
                <option :value="null"
                        v-text="$t('jobs.forwarding.selectPlaceholder')"
                        disabled></option>
            </template>
        </b-form-select>
        <b-button variant="primary"
                  v-text="$t('jobs.results.actions.forward')"
                  @click="forward"
                  class="mt-3"></b-button>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {ForwardingMode, Tool} from '@/types/toolkit/tools';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'ForwardingModal',
        components: {
            BaseModal,
        },
        props: {
            forwardingMode: {
                type: Object as () => ForwardingMode,
                required: true,
            },
            forwardingData: {
                type: String,
                required: true,
            },
        },
        data() {
            return {
                selectedTool: null,
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            toolOptions(): Tool[] {
                if (this.forwardingMode) {
                    const alignmentOptions: string[] = (this.forwardingMode as ForwardingMode).alignment;
                    return this.tools.filter((t: Tool) => alignmentOptions.includes(t.name));
                } else {
                    return [];
                }
            },
        },
        methods: {
            forward() {
                if (this.selectedTool) {
                    this.$router.push('/tools/' + this.selectedTool, () => {
                        EventBus.$emit('forward-data', this.forwardingData);
                        EventBus.$emit('hide-modal');
                    });
                }
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
