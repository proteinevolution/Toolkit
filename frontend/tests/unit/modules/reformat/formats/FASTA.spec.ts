import {expect} from 'chai';
import {FASTA} from '../../../../../src/modules/reformat/formats/FASTA';

describe('FASTA validation', () => {
    it('should detect any invalid characters', () => {
        expect(FASTA.validate('ABAB')).to.be.true;
        expect(FASTA.validate('ABAB@')).to.be.false;
    });
});
