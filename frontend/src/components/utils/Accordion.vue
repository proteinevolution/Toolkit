<template>
    <div role="tablist">
        <b-card v-for="item in items"
                :key="item.title"
                no-body
                class="mb-1">
            <b-card-header header-tag="header"
                           class="p-1"
                           role="tab">
                <b-btn block
                       href="#"
                       v-b-toggle="item.title"
                       variant="info">
                    {{ item.title }}
                </b-btn>
            </b-card-header>
            <b-collapse :id="item.title"
                        accordion="my-accordion"
                        role="tabpanel">
                <b-card-body>
                    <p v-html="item.content"></p>
                </b-card-body>
            </b-collapse>
        </b-card>
    </div>
</template>

<script lang="ts">
    export default {
        name: 'Accordion',
        props: {
            items: {
                // array of title-content-pairs from i18n
                type: Array,
                required: true,
            },
        },
    };
</script>

<style scoped>

</style>