import axios from 'axios';
import {Job, SimilarJobResult, SubmissionResponse} from '@/types/toolkit/jobs';

class JobService {

    public fetchJobs(): Promise<Job[]> {
        return new Promise<Job[]>((resolve, reject) => {
            axios.get('/api/jobs/')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public fetchJob(jobID: string): Promise<Job> {
        return new Promise<Job>(((resolve, reject) => {
            axios.get(`/api/jobs/${jobID}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public submitJob(toolName: string, submission: any): Promise<SubmissionResponse> {
        return new Promise<SubmissionResponse>((resolve, reject) => {
            axios.post(`/api/jobs/?toolName=${toolName}`, submission)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public logFrontendJob(toolName: string): Promise<void> {
        return new Promise<void>((resolve, reject) => {
            axios.get(`/api/jobs/?toolName=${toolName}`)
                .then(() => {
                    resolve();
                })
                .catch(reject);
        });
    }

    /**
     * Ask for delete of job. Job will get cleared over websockets as well.
     * @param jobID
     */
    public deleteJob(jobID: string): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.delete(`/api/jobs/${jobID}`)
                .then(() => {
                    resolve();
                })
                .catch(reject);
        }));
    }

    public getSimilarJob(jobID: string): Promise<SimilarJobResult> {
        return new Promise<SimilarJobResult>(((resolve, reject) => {
            axios.get(`/api/jobs/check/hash/${jobID}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public startJob(jobID: string): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.get(`/api/jobs/${jobID}/start`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public setJobPublic(jobID: string, isPublic: boolean): Promise<void> {
        return new Promise<void>(((resolve, reject) => {
            axios.put(`/api/jobs/${jobID}/`, {isPublic})
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }

    public suggestJobsForJobId(query: string): Promise<Job[]> {
        return new Promise<Job[]>(((resolve, reject) => {
            axios.get(`/api/jobs/suggest/${query}`)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        }));
    }
}

export const jobService = new JobService();
