<template>
    <div class="text-center img-container">
        <div v-for="(img, index) in images"
             :key="'img-' + index">
            <div v-if="labels[index]"
                 class="text-left border-bottom mb-3"
                 v-text="labels[index]"></div>
            <img :key="'img' + index"
                 :src="img"
                 class="plot-img"
                 alt=""
                 onerror="this.parentNode.classList.add('img-broken');">
            <span class="plot-img-alt"
                  v-text="altTexts[index]"></span>
        </div>
    </div>
</template>

<script lang="ts">
import ResultTabMixin from '@/mixins/ResultTabMixin';
import {resultsService} from '@/services/ResultsService';

export default ResultTabMixin.extend({
    name: 'ImagesViewTab',
    computed: {
        images(): string[] {
            if (!this.viewOptions.files) {
                return [];
            }
            const jobID: string = this.job.jobID;
            const files: string[] = this.viewOptions.files.split(';');
            return files.map((f: string) => resultsService.getDownloadFilePath(jobID, f));
        },
        labels(): string[] {
            if (!this.viewOptions.labels) {
                return [];
            }
            return this.viewOptions.labels.split(';');
        },
        altTexts(): string[] {
            if (!this.viewOptions.altTexts) {
                return [];
            }
            return this.viewOptions.altTexts.split(';')
                .map((key: string) => key ?
                    this.$t('jobs.results.imagesView.' + key) as string : '');
        },
    },
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
