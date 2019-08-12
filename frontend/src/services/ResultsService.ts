import axios, {AxiosResponse} from 'axios';
import {
    AlignmentResultResponse,
    HHInfoResult,
    SearchAlignmentItem,
    SearchAlignmentsResponse,
    SearchHitsResponse,
    StructureFileResponse,
} from '@/types/toolkit/results';

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

    public fetchAlignmentResults(jobId: string, start?: number, end?: number, resultField?: string):
        Promise<AlignmentResultResponse> {
        return new Promise<AlignmentResultResponse>((resolve, reject) => {
            const url: string = `/api/jobs/${jobId}/results/alignments/`;
            axios.get(url, {
                params: {
                    start,
                    end,
                    resultField,
                },
            })
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchHHAlignmentResults<T extends SearchAlignmentItem, S extends HHInfoResult>(jobId: string,
                                                                                          start?: number, end?: number):
        Promise<SearchAlignmentsResponse<T, S>> {
        return new Promise<SearchAlignmentsResponse<T, S>>((resolve, reject) => {
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
        Promise<SearchHitsResponse> {
        return new Promise<SearchHitsResponse>((resolve, reject) => {
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

    public generateTemplateAlignment(jobId: string, accession: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            axios.get(`/api/jobs/${jobId}/results/template-alignment/${accession}`)
                .then(() => resolve())
                .catch(reject);
        });
    }

    public generateForwardingData(jobId: string, params: any): Promise<string> {
        return new Promise<string>((resolve, reject) => {
            axios.get(`/api/jobs/${jobId}/results/forward-data/`, {params})
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public getStructureFile(accession: string): Promise<StructureFileResponse> {
        return new Promise<StructureFileResponse>((resolve, reject) => {
            axios.get(`/api/jobs/structure-file/${accession}`)
                .then((response) => {
                    resolve({data: response.data, filename: this.getResponseFilename(response)});
                })
                .catch(reject);
        });
    }

    private getResponseFilename(response: AxiosResponse): string | undefined {
        if (response.headers.hasOwnProperty('content-disposition')) {
            const header: string = response.headers['content-disposition'];
            const filenameIndex = header.indexOf('filename=');
            if (filenameIndex === -1) {
                return undefined;
            }
            const startIndex = filenameIndex + 10;
            const endIndex = header.length - 1;
            return header.substring(startIndex, endIndex);
        }
        return undefined;
    }
}

export const resultsService = new ResultsService();
