import {expect} from 'chai';
import {Reformat} from '@/modules/reformat';

const seqs: string = `
> Sequence 1
ABAB
> Sequence 2
ABABAB
`;

const seqs2: string = `
> Sequence 1
ABAB
> Sequence 2
ABAB
`;

const elem: Reformat = new Reformat(seqs);
const elem2: Reformat = new Reformat(seqs2);

describe('sameLength', () => {
    it('should validate sequences correctly', () => {
        expect(elem.sameLength()).to.be.false;
        expect(elem2.sameLength()).to.be.true;
    });
});



