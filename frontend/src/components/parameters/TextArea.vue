<template>
    <div>
        Test123
        <b-form>
            <b-form-group>
                <b-form-textarea id="alignment" :placeholder="parameter.input_placeholder">

                </b-form-textarea>
            </b-form-group>
        </b-form>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {TextAreaParameter} from '@/types/toolkit';

    export default Vue.extend({
        name: 'TextArea',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => TextAreaParameter,
        },
    });
</script>

<style scoped>

</style>