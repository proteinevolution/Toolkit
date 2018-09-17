import { expect } from 'chai';
import toolGetters from '@/store/modules/tools/getters.ts';
import {ToolState} from '@/store/types';

const rootState = {
    maintenanceMode: false,
    reconnecting: false,
};

describe('tools/getters', () => {
    it('should return the correct sections', () => {
        const state: ToolState = {
            tools: [
                {
                    name: 'Tool1', longname: 'Tool1', section: 'Section1', forwarding: {alignment: [], multiSeq: []},
                        validationParams: {}, showSubmitButtons: true,
                },
                {
                    name: 'Tool2', longname: 'Tool1', section: 'Section2', forwarding: {alignment: [], multiSeq: []},
                        validationParams: {}, showSubmitButtons: true,
                },
                {
                    name: 'Tool3', longname: 'Tool1', section: 'Section2', forwarding: {alignment: [], multiSeq: []},
                        validationParams: {}, showSubmitButtons: true,
                },
            ],
        };

        const result = toolGetters.sections(state, null, rootState, null);
        expect(result).to.deep.equal(['Section1', 'Section2']);
    });
});
