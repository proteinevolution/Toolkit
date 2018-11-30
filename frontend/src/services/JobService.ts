import axios from 'axios';
import {Job, SubmissionResponse} from '@/types/toolkit/jobs';

export default class JobService {

    public static fetchJobs(): Promise<Job[]> {
        return new Promise<Job[]>((resolve, reject) => {
            axios.get('/api/jobs/')
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

    public static submitJob(toolName: string, submission: any): Promise<SubmissionResponse> {
        return new Promise<SubmissionResponse>((resolve, reject) => {
            axios.post(`/api/jobs/?toolName=${toolName}`, submission)
                .then((response) => {
                    resolve(response.data);
                })
                .catch(reject);
        });
    }

}
