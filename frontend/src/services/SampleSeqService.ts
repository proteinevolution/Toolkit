import axios from 'axios';

class SampleSeqService {
    private sampleSeqs: { [key: string]: string } = {};

    public async fetchSampleSequence(sampleSequenceKey: string): Promise<string> {
        if (sampleSequenceKey in this.sampleSeqs) {
            return this.sampleSeqs[sampleSequenceKey];
        } else {
            const res = await axios.get<string>(`/sample-seqs/${sampleSequenceKey}`, {headers: {'Content-Type': 'text/plain'}});
            this.sampleSeqs[sampleSequenceKey] = res.data.trim();
            return this.sampleSeqs[sampleSequenceKey];
        }
    }
}

export const sampleSeqService = new SampleSeqService();
