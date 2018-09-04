import {expect} from 'chai';
import {Sequence} from '../../../../src/modules/reformat/types';
import {sameLength} from '../../../../src/modules/reformat/operations';
import {FASTA} from '../../../../src/modules/reformat/formats/FASTA';

const sequences: Sequence[] = [
    {
        identifier: 'Sequence1',
        seq: 'ABAB',
    },
    {
        identifier: 'Sequence2',
        seq: 'ABABAB',
    },
];

describe('sameLength', () => {
    it('should validate sequences correctly', () => {
        expect(sameLength.execute(sequences, '', FASTA)).to.be.false;
        expect(sameLength.execute(sequences.slice(1), '', FASTA)).to.be.true;
    });
});



