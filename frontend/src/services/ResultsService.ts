import axios, {AxiosResponse} from 'axios';
import {
    AlignmentResultResponse,
    ForwardingSubmission,
    HHInfoResult,
    SearchAlignmentItem,
    SearchAlignmentsResponse,
    SearchHitsResponse,
    StructureFileResponse,
} from '@/types/toolkit/results';

class ResultsService {
    public async fetchResults(jobId: string): Promise<any> {
        const res = await axios.get<any>(`/api/jobs/${jobId}/results/`);
        return res.data;
    }

    public async fetchAlignmentResults(jobId: string, start?: number, end?: number, resultField?: string):
        Promise<AlignmentResultResponse> {
        const res = await axios.get<AlignmentResultResponse>(`/api/jobs/${jobId}/results/alignments/`, {
            params: {
                start,
                end,
                resultField,
            },
        });
        return res.data;
    }

    public async fetchHHAlignmentResults<T extends SearchAlignmentItem, S extends HHInfoResult>(
        jobId: string,
        start?: number,
        end?: number
    ): Promise<SearchAlignmentsResponse<T, S>> {
        const res = await axios.get<SearchAlignmentsResponse<T, S>>(`/api/jobs/${jobId}/results/hh-alignments/`, {
            params: {
                start,
                end,
            },
        });
        return res.data;
    }

    public async fetchHits(jobId: string, start?: number, end?: number, filter?: string, sortBy?: string, desc?: boolean):
        Promise<SearchHitsResponse> {
        const res = await axios.get<SearchHitsResponse>(`/api/jobs/${jobId}/results/hits/`, {
            params: {
                start,
                end,
                filter,
                sortBy,
                desc,
            },
        });
        return res.data;
    }

    public getDownloadFilePath(jobId: string, file: string): string {
        return `/api/jobs/${jobId}/results/files/${file}`;
    }

    public async getFile(jobId: string, file: string): Promise<any> {
        const res = await axios.get<any>(this.getDownloadFilePath(jobId, file));
        return res.data;
    }

    public async downloadFile(jobId: string, file: string, downloadFilename: string): Promise<void> {
        const res = await axios.get<string>(this.getDownloadFilePath(jobId, file));
        this.downloadAsFile(res.data, downloadFilename);
    }

    public downloadAsFile(file: string, downloadFilename: string): void {
        const blob = new Blob([file], {type: 'application/octet-stream'});
        if ((window as any).navigator.msSaveOrOpenBlob) {
            (window as any).navigator.msSaveBlob(blob, downloadFilename);
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

    public async generateTemplateAlignment(jobId: string, accession: string): Promise<void> {
        await axios.get(`/api/jobs/${jobId}/results/template-alignment/${accession}`);
    }

    public async generateForwardingData(jobId: string, submission: ForwardingSubmission): Promise<string> {
        const res = await axios.post<string>(`/api/jobs/${jobId}/results/forward-data/`, submission);
        return res.data;
    }

    public async getStructureFile(accession: string): Promise<StructureFileResponse> {
        const res = await axios.get<StructureFileResponse>(`/api/jobs/structure-file/${accession}`);
        return {data: res.data, filename: ResultsService.getResponseFilename(res)};
    }

    private static getResponseFilename(response: AxiosResponse): string | undefined {
        if ('content-disposition' in response.headers) {
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
