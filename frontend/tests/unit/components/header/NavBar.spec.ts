import {expect} from 'chai';
import BootstrapVue from 'bootstrap-vue';
import {createLocalVue, shallowMount} from '@vue/test-utils';
import NavBar from '@/components/navigation/NavBar.vue';
import {tools1} from '../../mocks/tools';

const localVue = createLocalVue();
localVue.use(BootstrapVue);

const sections: string[] = [];

function initNavBar(toolNameRouteParam: string | undefined) {
    const $route = {
        params: {
            toolName: toolNameRouteParam,
        },
    };

    return shallowMount(NavBar, {
        localVue,
        mocks: {
            $route,
            $t: (arg: string) => arg,
        },
        computed: {
            tools: () => tools1,
            jobs: () => [],
            sections: () => sections,
            sectionColor: () => '#FFFFFF',
        },
    });
}

/*
Note: Somehow finding an element by class doesn't work...
        expect(wrapper.find('.active').text()).to.contain('Section1');
        --> Error: find did not return .active, cannot call text() on empty Wrapper
 */

describe('NavBar', () => {
    it('selects correct section if route parameter is set', () => {
        const wrapper = initNavBar('Tool2');
        expect((wrapper.vm as any).selectedSection).to.equal('Section2');
    });

    it('selects first section if invalid route parameter is set', () => {
        const wrapper = initNavBar('ToolXYZ');
        expect((wrapper.vm as any).selectedSection).to.equal('search');
    });

    it('selects first section if no route parameter is set', () => {
        const wrapper = initNavBar(undefined);
        expect((wrapper.vm as any).selectedSection).to.equal('search');
    });

    it('updates manually selected selection correctly', () => {
        const wrapper = initNavBar(undefined);
        (wrapper.vm as any).selectSection('Section2');
        expect((wrapper.vm as any).selectedSection).to.equal('Section2');
        (wrapper.vm as any).selectSection('Section1');
        expect((wrapper.vm as any).selectedSection).to.equal('Section1');
    });
});
