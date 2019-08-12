import {expect} from 'chai';
import {isProteinSequence} from '@/modules/reformat/sequenceTypes/PROTEIN';

describe('PROTEIN validation', () => {
    it('should detect invalid characters', () => {
        expect(isProteinSequence('AGAG')).to.be.true;
        expect(isProteinSequence('AGAG.--.DA')).to.be.true;
        expect(isProteinSequence('AGAG@')).to.be.false;
    });
});
