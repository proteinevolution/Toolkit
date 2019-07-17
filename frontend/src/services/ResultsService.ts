import axios from 'axios';
import {AlignmentItem} from '@/types/toolkit/results';

class ResultsService {

    public fetchResults(jobId: string): Promise<any> {
        return new Promise<any>((resolve, reject) => {
            axios.get(`/api/jobs/${jobId}/results/`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchAlignmentResults(jobId: string): Promise<AlignmentItem[]> {
        return new Promise<AlignmentItem[]>((resolve, reject) => {
            axios.get(`/api/jobs/${jobId}/results/alignment/`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public getDownloadFilePath(jobId: string, file: string): string {
        return `/api/jobs/${jobId}/results/files/${file}`;
    }

    public downloadFile(jobId: string, file: string, downloadFilename: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            axios.get(this.getDownloadFilePath(jobId, file))
                .then((response) => {
                    const blob = new Blob([response.data], {type: 'application/octet-stream'});
                    if (window.navigator.msSaveOrOpenBlob) {
                        window.navigator.msSaveBlob(blob, downloadFilename);
                    } else {
                        const a = document.createElement('a');
                        a.href = URL.createObjectURL(blob);
                        a.download = downloadFilename;
                        document.body.appendChild(a);
                        a.click();
                        URL.revokeObjectURL(a.href);
                        a.remove();
                    }
                    resolve();
                })
                .catch(reject);
        });
    }
}

export const resultsService = new ResultsService();
