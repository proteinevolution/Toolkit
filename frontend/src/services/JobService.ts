import axios from 'axios';
import {Job} from '@/types/toolkit/jobs';

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

}
