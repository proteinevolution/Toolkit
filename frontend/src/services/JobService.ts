import axios from 'axios';
import {Job, SimilarJobResult, SubmissionResponse} from '@/types/toolkit/jobs';

class JobService {
    public async fetchJobs(): Promise<Job[]> {
        const res = await axios.get<Job[]>('/api/jobs/');
        return res.data;
    }

    public async fetchJob(jobID: string): Promise<Job> {
        const res = await axios.get<Job>(`/api/jobs/${jobID}`);
        return res.data;
    }

    public async submitJob(toolName: string, submission: any): Promise<SubmissionResponse> {
        const res = await axios.post<SubmissionResponse>(`/api/jobs/?toolName=${toolName}`, submission);
        return res.data;
    }

    public async logFrontendJob(toolName: string): Promise<void> {
        await axios.get(`/api/jobs/?toolName=${toolName}`);
    }

    /**
     * Ask for delete of job. Job will get cleared over websockets as well.
     * @param jobID
     */
    public async deleteJob(jobID: string): Promise<void> {
        await axios.delete(`/api/jobs/${jobID}`);
    }

    public async getSimilarJob(jobID: string): Promise<SimilarJobResult> {
        const res = await axios.get<SimilarJobResult>(`/api/jobs/check/hash/${jobID}`);
        return res.data;
    }

    public async startJob(jobID: string): Promise<void> {
        await axios.get(`/api/jobs/${jobID}/start`);
    }

    public async setJobPublic(jobID: string, isPublic: boolean): Promise<void> {
        await axios.put(`/api/jobs/${jobID}/`, {isPublic});
    }

    public async suggestJobsForJobId(query: string): Promise<Job[]> {
        const res = await axios.get<Job[]>(`/api/jobs/suggest/${query}`);
        return res.data;
    }
}

export const jobService = new JobService();
