import axios from 'axios';

class SampleSeqService {

    private sampleSeqs: { [key: string]: string } = {};

    public fetchSampleSequence(sampleSequenceKey: string): Promise<string> {
        return new Promise<string>((resolve, reject) => {
            if (this.sampleSeqs.hasOwnProperty(sampleSequenceKey)) {
                resolve(this.sampleSeqs[sampleSequenceKey]);
            } else {
                axios.get(`/sample-seqs/${sampleSequenceKey}`)
                    .then((response) => {
                        this.sampleSeqs[sampleSequenceKey] = response.data.trim();
                        resolve(this.sampleSeqs[sampleSequenceKey]);
                    })
                    .catch(reject);
            }
        });
    }
}

export const sampleSeqService = new SampleSeqService();
(window as any).sampleSeqService = sampleSeqService;
