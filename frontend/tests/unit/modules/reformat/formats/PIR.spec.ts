import {expect} from 'chai';
import {PIR} from '@/modules/reformat/formats/PIR';

const valid = `
>XY;ABC
Description
ABCDEF.-*
`;

const invalidHeader = `
>XYABC
Description
ABCDEF.-*
`;

const invalidHeader2 = `
XY;ABC
Description
ABCDEF.-*
`;

const missingDescription = `
>XY;ABC
ABCDEF.-*
`;

const invalidSequence = `
>XY;ABC
Description
ABCDEF@*
`;


// tslint:disable:max-line-length
const example1: string = `>P1;UKNP
sequence:UKNP:1    :A:132  :A::::
MSGRGKQGG-KARAKAKTRSSRAGLQFPVGRVHRLLRKGNY-AERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK*
>P1;4WNN
structure:4WNN:18  :C:100 :C::Saccharomyces cerevisiae:1.8:
-----------------SRSAKAGLTFPVGRVHRLLRRGNY-AQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLG-------------------------------*
>P1;4CAY
structure:4CAY:2   :A:91  :A::HOMO SAPIENS:1.48:
-----------------SRSQRAGLQFPVGRIHRHLKSR----GRVGATAAVYSAAILEYLTAEVLELAGNASKDLKVKRITPRHLQLAIRGDEELDSLIK-ATIAGG------------------------*
>P1;5B0Z
structure:5B0Z:15  :C:122 :C::Homo sapiens:1.987:
------------RAKAKTRSSRAGLQFPVGRVHRLLRKGNY-SERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGRVTIAQGGVLPNIQAVLLPK-----------*`;


describe('PIR validation', () => {
    it('should detect invalid headers, sequences and descriptions', () => {
        expect(PIR.validate(valid)).to.be.true;
        expect(PIR.validate(invalidHeader)).to.be.false;
        expect(PIR.validate(invalidHeader2)).to.be.false;
        expect(PIR.validate(invalidSequence)).to.be.false;
        expect(PIR.validate(missingDescription)).to.be.false;
    });

    it('should validate example', () => {
        expect(PIR.validate(example1)).to.be.true;
    });
});

describe('PIR reading', () => {
    it('should remove trailing star', () => {
        expect(PIR.read(valid)[0].seq).to.equal('ABCDEF.-');
    });
});

