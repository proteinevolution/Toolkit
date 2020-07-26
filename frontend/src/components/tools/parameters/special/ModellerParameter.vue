<template>
    <div>
        <b-form-group v-if="!valid"
                      :label="$t('tools.parameters.labels.' + parameter.name)">
            <b-form-input v-model="submissionValue"
                          type="text"
                          size="sm"
                          :state="submissionValue.length > 0 ? valid : null"
                          required />
        </b-form-group>
        <p v-else
           v-text="$t('tools.parameters.modellerKey.stored')"></p>
    </div>
</template>

<script lang="ts">
    import {authService} from '@/services/AuthService';
    import {debounce} from 'lodash-es';
    import mixins from 'vue-typed-mixins';
    import {Parameter} from '@/types/toolkit/tools';
    import ToolParameterMixin from '@/mixins/ToolParameterMixin';
    import {ConstraintError} from '@/types/toolkit/validation';
    import {User} from '@/types/toolkit/auth';

    export default mixins(ToolParameterMixin).extend({
        name: 'ModellerParameter',
        props: {
            /*
             Simply stating the interface type doesn't work, this is a workaround. See
             https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
             */
            parameter: Object as () => Parameter,
        },
        data() {
            return {
                valid: null,
            };
        },
        computed: {
            defaultSubmissionValue(): any {
                // overrides the property in ToolParameterMixin
                return '';
            },
            user(): User | null {
                return this.$store.getters['auth/user'];
            },
        },
        watch: {
            submissionValue: {
                immediate: true,
                handler(value: string) {
                    this.valid = null;
                    this.validateModellerKey(value);
                },
            },
            user() {
                this.validateModellerKey(this.submissionValue);
            },
        },
        methods: {
            validateModellerKey: debounce(function(this: any, value: string) {
                authService.validateModellerKey(value)
                    .then((result: boolean) => {
                        const error: ConstraintError | undefined = result ? undefined : {
                            textKey: 'constraints.invalidModellerKey',
                        };
                        this.setError(error);

                        this.valid = result;
                    });
            }, 500),
        },
    });
</script>
