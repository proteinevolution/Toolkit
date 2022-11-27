import { expect } from 'chai';
import BootstrapVue from 'bootstrap-vue';
import { shallowMount } from '@vue/test-utils';
import { ParameterType } from '@/types/toolkit/enums';
import { SelectOption } from '@/types/toolkit/tools';
import SelectParameter from '@/components/tools/parameters/SelectParameter.vue';

const options: SelectOption[] = [
    {
        value: 'option1',
        text: 'Option1',
    },
    {
        value: 'option2',
        text: 'Option2',
    },
    {
        value: 'option3',
        text: 'Option3',
    },
];

function initSelectParameter(maxSelectedOptions: number, submission?: string) {
    return shallowMount(SelectParameter, {
        global: {
            plugins: [BootstrapVue],
            mocks: {
                $t: (arg: string) => arg,
            },
        },
        props: {
            submission: {
                select: submission ? submission : '',
            },
            validationErrors: {},
            rememberParams: {},
            parameter: {
                parameterType: ParameterType.SelectParameter,
                name: 'select',
                label: 'Test',
                maxSelectedOptions,
                options,
            },
        },
    });
}

describe('SelectParameter', () => {
    it('sets the correct submission values for single selection', () => {
        const wrapper = initSelectParameter(1);
        wrapper.setData({ selected: options[0] });
        expect(wrapper.props('submission').select).to.equal('option1');
        wrapper.setData({ selected: options[1] });
        expect(wrapper.props('submission').select).to.equal('option2');
    });

    it('sets the correct submission values for multi selection', () => {
        const wrapper = initSelectParameter(2);
        wrapper.setData({ selected: options[0] });
        expect(wrapper.props('submission').select).to.equal('option1');
        wrapper.setData({ selected: options.slice(0, 2) });
        expect(wrapper.props('submission').select).to.equal('option1 option2');
    });

    it('uses existing submission for single selection', () => {
        const wrapper = initSelectParameter(1, 'option2');
        expect((wrapper.vm as any).selected).to.deep.equal(options.slice(1, 2));
    });

    it('uses existing submission for multi selection', () => {
        const wrapper = initSelectParameter(2, 'option1 option3');
        expect((wrapper.vm as any).selected).to.deep.equal([options[0], options[2]]);
    });
});
