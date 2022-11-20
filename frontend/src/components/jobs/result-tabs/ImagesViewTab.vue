<template>
    <div class="text-center img-container">
        <div v-for="(img, index) in images" :key="'img-' + index">
            <div v-if="labels[index]" class="text-left border-bottom mb-3" v-text="labels[index]"></div>
            <img
                :key="'img' + index"
                :src="img"
                class="plot-img"
                alt=""
                onerror="this.parentNode.classList.add('img-broken');" />
            <span class="plot-img-alt" v-text="altTexts[index]"></span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { defineResultTabProps } from '@/composables/useResultTab';
import { resultsService } from '@/services/ResultsService';
import { isNullable } from '@/util/nullability-helpers';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineResultTabProps();

const images = computed(() => {
    const filesString = props.viewOptions?.files;
    if (isNullable(filesString)) {
        return [];
    }
    const jobID: string = props.job.jobID;
    return filesString.split(';').map((f: string) => resultsService.getDownloadFilePath(jobID, f));
});

const labels = computed(() => {
    const labelsString = props.viewOptions?.labels;
    if (isNullable(labelsString)) {
        return [];
    }
    return labelsString.split(';');
});

const altTexts = computed(() => {
    const altTextsString = props.viewOptions?.altTexts;
    if (isNullable(altTextsString)) {
        return [];
    }
    return altTextsString.split(';').map((key: string) => (key ? (t('jobs.results.imagesView.' + key) as string) : ''));
});
</script>

<style lang="scss" scoped>
.img-container {
    overflow-x: auto;
}

.plot-img {
    margin-bottom: 2rem;
}

.plot-img-alt {
    display: none;
}

.img-broken {
    .plot-img {
        display: none;
    }

    .plot-img-alt {
        display: block;
    }
}
</style>
