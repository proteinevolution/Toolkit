import axios from 'axios';
import {AlignmentItem} from '@/types/toolkit/jobs';

class ResultsService {

    public fetchAlignmentResults(jobId: string):
        Promise<AlignmentItem[]> {
        return new Promise<AlignmentItem[]>((resolve, reject) => {
            axios.post(`/api/jobs/${jobId}/results/alignment/`, {resultName: 'alignment'})
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }
}

export const resultsService = new ResultsService();
