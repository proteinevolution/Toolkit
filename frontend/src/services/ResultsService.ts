import axios from 'axios';
import {AlignmentItem, AlignmentResultResponse, ResultHitsResponse} from '@/types/toolkit/results';

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

    public fetchAlignmentResults(jobId: string, start?: number, end?: number): Promise<AlignmentResultResponse> {
        return new Promise<AlignmentResultResponse>((resolve, reject) => {
            const url: string = `/api/jobs/${jobId}/results/alignments/`;
            axios.get(url, {
                params: {
                    start,
                    end,
                },
            })
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchHHAlignmentResults(jobId: string, start?: number, end?: number): Promise<AlignmentResultResponse> {
        return new Promise<AlignmentResultResponse>((resolve, reject) => {
            const url: string = `/api/jobs/${jobId}/results/hh-alignments/`;
            axios.get(url, {
                params: {
                    start,
                    end,
                },
            })
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchHits(jobId: string, start?: number, end?: number, filter?: string, sortBy?: string, desc?: boolean):
        Promise<ResultHitsResponse> {
        return new Promise<ResultHitsResponse>((resolve, reject) => {
            const url: string = `/api/jobs/${jobId}/results/hits/`;
            axios.get(url, {
                params: {
                    start,
                    end,
                    filter,
                    sortBy,
                    desc,
                },
            })
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public getDownloadFilePath(jobId: string, file: string): string {
        return `/api/jobs/${jobId}/results/files/${file}`;
    }

    public getFile(jobId: string, file: string): Promise<any> {
        return new Promise<any>((resolve, reject) => {
            axios.get(this.getDownloadFilePath(jobId, file))
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public downloadFile(jobId: string, file: string, downloadFilename: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            axios.get(this.getDownloadFilePath(jobId, file))
                .then((response) => {
                    this.downloadAsFile(response.data, downloadFilename);
                    resolve();
                })
                .catch(reject);
        });
    }

    public downloadAsFile(file: string, downloadFilename: string): void {
        const blob = new Blob([file], {type: 'application/octet-stream'});
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
    }
}

export const resultsService = new ResultsService();
