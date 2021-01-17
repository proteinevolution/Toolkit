<template>
    <b-modal :id="id"
             centered
             hide-header
             hide-footer
             no-fade
             :size="size"
             :body-class="bodyClass"
             :modal-class="'tk-modal ' + modalClass"
             :lazy="lazy"
             :static="static"
             scrollable
             @hide="$emit('hide')"
             @show="$emit('show')"
             @hidden="$emit('hidden')"
             @shown="$emit('shown')">
        <slot name="header">
            <div class="tk-modal-header">
                <span class="tk-modal-title"
                      v-html="title"></span>

                <span class="tk-modal-close"
                      @click="$root.$emit('bv::hide::modal',id)">
                    &times;
                </span>
            </div>
        </slot>
        <slot name="body">
            <div class="tk-modal-content">
                <slot></slot>
            </div>
        </slot>
    </b-modal>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
    name: 'BaseModal',
    inheritAttrs: true,
    props: {
        id: String,
        title: String,
        size: {
            type: String,
            required: false,
            default: 'lg',
        },
        bodyClass: {
            type: String,
            required: false,
            default: '',
        },
        modalClass: {
            type: String,
            required: false,
            default: '',
        },
        lazy: {
            type: Boolean,
            required: false,
            default: true,
        },
        static: {
            type: Boolean,
            required: false,
            default: false,
        },
    },
});
</script>

<style lang="scss">
.tk-modal .modal-body {
  padding: 1.5rem 2rem;
  box-shadow: 0 20px 60px -2px rgba(27, 33, 58, 0.4);

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

  .tk-modal-content {
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
