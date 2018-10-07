<template>
    <div class="tk-modal">
        <div class="tk-modal-header">
            <span class="tk-modal-title"
                  v-html="title">
            </span>

            <span class="tk-modal-close"
                  @click="$emit('close')">
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
    });
</script>

<style lang="scss">
    .tk-modal {
        padding: 1.5rem 2rem;

        .tk-modal-header {
            display: flex;
            margin-bottom: 1.5rem;

            .tk-modal-title {
                color: $primary;
                font-size: 1.625em;
            }

            .tk-modal-close {
                padding-right: 5px;
                font-size: 1.625rem;
                margin-left: auto;
                line-height: 1;
                color: $tk-gray;
                cursor: pointer;
            }
        }

        .tk-modal-body {
            color: $tk-dark-gray;

            .section {
                margin-top: 1.5rem;

                h6 {
                    font-weight: bold;
                    font-size: 1em;
                    margin-bottom: 0.25em;
                }
            }
        }
    }
</style>
