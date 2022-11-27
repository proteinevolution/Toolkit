import { expect } from 'chai';
import { shallowMount } from '@vue/test-utils';
import { ParameterType } from '@/types/toolkit/enums';
import BooleanParameter from '@/components/tools/parameters/BooleanParameter.vue';
import BootstrapVue from 'bootstrap-vue';

function initBooleanParameter(defaultValue: boolean, enabledOverride: boolean) {
    return shallowMount(BooleanParameter, {
        global: {
            plugins: [BootstrapVue],
            mocks: {
                $t: (arg: string) => arg,
            },
        },
        props: {
            submission: {},
            validationErrors: {},
            rememberParams: {},
            parameter: {
                parameterType: ParameterType.BooleanParameter,
                name: 'Test',
                label: 'Test',
                default: defaultValue,
            },
            enabledOverride,
        },
    });
}

describe('BooleanParameter', () => {
    it('sets correct default submission value', () => {
        const wrapper1 = initBooleanParameter(true, false);
        expect((wrapper1.vm as any).submissionValue).to.equal(true);
        const wrapper2 = initBooleanParameter(false, false);
        expect((wrapper2.vm as any).submissionValue).to.equal(false);
        const wrapper3 = initBooleanParameter(false, true);
        expect((wrapper3.vm as any).submissionValue).to.equal(true);
    });
});
