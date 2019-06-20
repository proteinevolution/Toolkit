import {expect} from 'chai';
import {STOCKHOLM} from '@/modules/reformat/formats/STOCKHOLM';

const valid = `
# STOCKHOLM 1.0
# ABCD@123
test\tAaZz-.*
`;

const missingHeader = `
test\tAaZz-.*
`;

const wrongHeader = `
# STOCKHOLM 1.1
test\tAaZz-.*
`;

const emptySequence = `
# STOCKHOLM 1.0
test\t
`;

const invalidChars = `
# STOCKHOLM 1.0
test\tABCD@EFGH
`;

const endOfAlignment = `
# STOCKHOLM 1.0
test\tABCDE
//
test\tABCD@EFGH
`;

const example1: string = `# STOCKHOLM 1.0
#GF SQ 8
#GF gi|33300828 DE gi|33300828
gi|33300828\tPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHID---EAMPNYGRIVEVSAMERSAN-TLRHPSFSRFR
#GF gi|11479639 DE gi|11479639
gi|11479639\tPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGGA-DHPGMADLGRVVEVTAMERSAN-TLRHPKFSRFR
#GF gi|11479645 DE gi|11479645
gi|11479645\tPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAYEVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGFR
#GF gi|29366706 DE gi|29366706
gi|29366706\tPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPG-------YYKGHTAKVTFMERYPDGSLRHPSFDSFR
#GF gi|68299729 DE gi|68299729
gi|68299729\tPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGHD-----DCFNGRPVQVKYMEKTPKGSLRHPSFQRWR
#GF gi|77118174 DE gi|77118174
gi|77118174\tPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP-------DYYKGWACQITYMEETPDGSLRHPSFDQWR
#GF gi|17570796 DE gi|17570796
gi|17570796\tPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGED------FYNGWACQVNYMEATPDGSLRHPSFEKFR
#GF gi|11963775 DE gi|11963775
gi|11963775\tPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGED------YYNGWACQVAYMEETSDGSLRHPSFVMFR
`;

describe('STOCKHOLM validation', () => {
    it('should validate header correctly', () => {
        expect(STOCKHOLM.validate(missingHeader)).to.be.false;
        expect(STOCKHOLM.validate(wrongHeader)).to.be.false;
    });

    it('should validate sequences correctly', () => {
        expect(STOCKHOLM.validate(valid)).to.be.true;
        expect(STOCKHOLM.validate(emptySequence)).to.be.false;
        expect(STOCKHOLM.validate(invalidChars)).to.be.false;
        expect(STOCKHOLM.validate(endOfAlignment)).to.be.true;
    });

    it('should validate example', () => {
        expect(STOCKHOLM.validate(example1)).to.be.true;
    });
});

describe('STOCKHOLM reading', () => {
    it('should read example correctly', () => {
        const read = STOCKHOLM.read(example1);
        expect(read.length).to.equal(8);
    });
});
