<template>
    <div class="tk-modal">
        <div class="tk-modal-header">
            <span class="tk-modal-title">
                {{title}}
            </span>

            <span class="tk-modal-close d-none"
                  @click="close">
                &times;
            </span>
        </div>
        <div class="tk-modal-body">
            <slot></slot>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';

    export default Vue.extend({
        name: 'BaseModal',
        props: {
            title: String,
        },
        methods: {
            close(): void {
                // TODO: make it closable
            },
        },
    });
</script>

<style lang="scss" scoped>
    .tk-modal {
        .tk-modal-header {
            padding: 0.7rem 1rem;
            display: flex;

            .tk-modal-title {
                color: $primary;
                font-size: 1.5em;
            }

            .tk-modal-close {
                padding-right: 5px;
                font-size: 1.5rem;
                margin-left: auto;
                line-height: 1;
                color: $tk-gray;
                cursor: pointer;
            }
        }

        .tk-modal-body {
            padding: 0.75rem 1rem;
        }
    }
</style>
