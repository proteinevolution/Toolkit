<template>
    <div role="tablist">
        <div v-for="item in items"
             :key="item.title"
             class="card mb-2">
            <b-card-header class="p-1 header" role="tab">
                <b-btn block
                       href="#"
                       v-b-toggle="item.title"
                       variant="link"
                       class="button">
                        <i class="icon fas fa-angle-right mr-2"></i>
                    <b>{{ item.title }}</b>
                </b-btn>
            </b-card-header>
            <b-collapse :id="item.title"
                        accordion="my-accordion"
                        role="tabpanel">
                    <div v-html="item.content"
                         class="content">
                    </div>
            </b-collapse>
        </div>
    </div>
</template>

<script lang="ts">
    import {AccordionItem} from '@/types/toolkit/utils';

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