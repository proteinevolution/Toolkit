<template>
    <div v-if="errorMessage" class="page-not-found">
        <div class="error-message border text-muted mt-5">
            <i class="fas fa-exclamation-triangle pl-3"></i>
            <span class="ml-2" v-html="$t(errorMessage)"></span>
        </div>
        <div class="border banner-area text-center scrolling-wrapper">
            <span v-html="alignment"></span>
            <span class="banner-message">You have reached a domain of unknown function!</span>
            <br /><br /><br />
        </div>
    </div>
</template>

<script lang="ts" setup>
import useToolkitTitle from '../../hooks/useToolkitTitle';
import { colorSequence } from '@/util/SequenceUtils';

const props = defineProps({
    errorMessage: {
        type: String,
        required: false,
        default: 'errors.PageNotFound',
    },
    title: String,
});

useToolkitTitle(props.title ?? '404');

const aminoAcids = ['A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y'];

let alignment =
    '<br><br><br>' +
    '       AAAAAAAAA         QQQQQQQQQ            AAAAAAAAA    <br>' +
    '      AGGGGGGGGA       QQGGGGGGGGGQQ         AGGGGGGGGA    <br>' +
    '     AGGGGGGGGGA     QQGGGGGGGGGGGGGQQ      AGGGGGGGGGA    <br>' +
    '    AGGGGAAGGGGA    QGGGGGGGQQQGGGGGGGQ    AGGGGAAGGGGA    <br>' +
    '   AGGGGA AGGGGA    QGGGGGGQ   QGGGGGGQ   AGGGGA AGGGGA    <br>' +
    '  AGGGGA  AGGGGA    QGGGGGQ     QGGGGGQ  AGGGGA  AGGGGA    <br>' +
    ' AGGGGA   AGGGGA    QGGGGGQ     QGGGGGQ AGGGGA   AGGGGA    <br>' +
    'AGGGGAAAAAAGGGGAAAAAQGGGGGQ     QGGGGGQAGGGGAAAAAAGGGGAAAAA<br>' +
    'AGGGGGGGGGGGGGGGGAAAQGGGGGQ     QGGGGGQAGGGGGGGGGGGGGGGGAAA<br>' +
    'AAAAAAAAAAGGGGGAAAAAQGGGGGQ     QGGGGGQAAAAAAAAAAGGGGGAAAAA<br>' +
    '          AGGGGA    QGGGGGQ     QGGGGGQ          AGGGGA    <br>' +
    '          AGGGGA    QGGGGGGQ   QGGGGGGQ          AGGGGA    <br>' +
    '          AGGGGA    QGGGGGGGQQQGGGGGGGQ          AGGGGA    <br>' +
    '        AAGGGGGGAA   QQGGGGGGGGGGGGGQQ         AAGGGGGGAA  <br>' +
    '        AGGGGGGGGA     QQGGGGGGGGGQQ           AGGGGGGGGA  <br>' +
    '        AAAAAAAAAA       QQQQQQQQQ             AAAAAAAAAA  <br>';

alignment = alignment.replace(/A/g, aminoAcids[Math.floor(Math.random() * 19 + 1)]);
alignment = alignment.replace(/Q/g, aminoAcids[Math.floor(Math.random() * 19 + 1)]);

for (let i = 0; i < alignment.length; i++) {
    if (alignment[i] === 'G') {
        alignment =
            alignment.substring(0, i) + aminoAcids[Math.floor(Math.random() * 19 + 1)] + alignment.substring(i + 1);
    }
}

alignment = '<pre>' + colorSequence(alignment) + '</pre>';
</script>

<style lang="scss">
.error-message {
    border-radius: $global-radius;
    height: 45px;
    line-height: 45px;
    background-color: #f2f2f2;

    span {
        font-size: 1em;
    }
}

.banner-area {
    font-family: $font-family-monospace;
    font-size: 0.85em;
    margin-top: -2px;
    background-color: white;
    border-radius: $global-radius;

    .banner-message {
        font-family: $font-family-sans-serif;
        font-size: 1.2em;
        color: #7d7d7d;
    }
}

.scrolling-wrapper {
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
}
</style>
