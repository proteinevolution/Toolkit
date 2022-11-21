<template>
    <div ref="el" class="observer"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

const props = defineProps({
    options: {
        type: Object,
        required: false,
        default: () => ({}),
    },
});
const emit = defineEmits(['intersect']);

const observer = ref<IntersectionObserver | null>(null);
const el = ref();

onMounted(() => {
    observer.value = new IntersectionObserver(([entry]: any) => {
        if (entry && entry.isIntersecting) {
            emit('intersect');
        }
    }, props.options);

    observer.value.observe(el.value);
});

onBeforeUnmount(() => {
    observer.value?.disconnect();
});
</script>
