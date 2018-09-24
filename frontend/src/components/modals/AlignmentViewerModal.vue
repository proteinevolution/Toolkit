<template>
    <BaseModal :title="'AlignmentViewer'">
        <AlignmentViewer :sequences="sequences"
                         :format="format">
        </AlignmentViewer>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';

    export default Vue.extend({
        name: 'AlignmentViewerModal',
        components: {
            BaseModal,
            AlignmentViewer,
        },
        props: {
            sequences: {
                type: String,
                required: true,
            },
            format: {
                type: String,
                required: true,
            },
        },
    });
</script>
