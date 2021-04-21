import {expect} from 'chai';
import BootstrapVue from 'bootstrap-vue';
import {createLocalVue, shallowMount} from '@vue/test-utils';
import NavBar from '@/components/navigation/NavBar.vue';
import {tools1} from '../../mocks/tools';
import {sections} from '@/conf/ToolSections';

const localVue = createLocalVue();
localVue.use(BootstrapVue);

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
            user: () => null,
            isAdmin: () => false,
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
        const wrapper = initNavBar(tools1[1].name);
        expect((wrapper.vm as any).selectedSection).to.equal(tools1[1].section);
    });

    it('selects first section if invalid route parameter is set', () => {
        const wrapper = initNavBar('ToolXYZ');
        expect((wrapper.vm as any).selectedSection).to.equal(sections[0]);
    });

    it('selects first section if no route parameter is set', () => {
        const wrapper = initNavBar(undefined);
        expect((wrapper.vm as any).selectedSection).to.equal(sections[0]);
    });

    it('updates manually selected selection correctly', () => {
        const wrapper = initNavBar(undefined);
        (wrapper.vm as any).selectSection(tools1[1].section);
        expect((wrapper.vm as any).selectedSection).to.equal(tools1[1].section);
        (wrapper.vm as any).selectSection(tools1[0].section);
        expect((wrapper.vm as any).selectedSection).to.equal(tools1[0].section);
    });
});
