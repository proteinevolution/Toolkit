<template>
    <div role="tablist">
        <b-card v-for="(item, i) in items" :key="i" no-body class="mb-2">
            <b-card-header class="p-1 header" role="tab">
                <b-btn v-b-toggle="'accordion-' + i" block variant="link" class="button">
                    <i class="icon fas fa-angle-right mr-2"></i>
                    <b>{{ item.title }}</b>
                </b-btn>
            </b-card-header>
            <b-collapse :id="'accordion-' + i" accordion="help-accordion" role="tabpanel">
                <div class="content" v-html="item.content"></div>
            </b-collapse>
        </b-card>
    </div>
</template>

<script lang="ts">
import { AccordionItem } from '@/types/toolkit/utils';

export default {
    name: 'Accordion',
    props: {
        /*
         Simply stating the interface type doesn't work, this is a workaround. See
         https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
         */
        items: {
            type: Array as () => AccordionItem[],
            required: true,
        },
    },
};
</script>

<style lang="scss" scoped>
.card {
    border-radius: 0;
    border: 1px solid $tk-lighter-gray;
}

.button {
    text-align: left;
    color: $tk-gray;
    text-decoration: none !important;
}

.header {
    background: none;
    border: none;
}

.content {
    padding: 0.5rem 2rem 1rem 2rem;
}

.icon {
    transition: all 0.2s ease;
}

:not(.collapsed) > .icon {
    -webkit-transform: rotate(90deg);
    -moz-transform: rotate(90deg);
    -o-transform: rotate(90deg);
    -ms-transform: rotate(90deg);
    transform: rotate(90deg);
}
</style>
