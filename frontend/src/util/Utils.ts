import {ProcessLogItem} from '@/types/toolkit/jobs';

export function parseProcessLog(file: string): ProcessLogItem[] {
    return file.split('#')
        .filter((val: string) => val.trim() !== '')
        .map((val: string) => {
            const split = val.split('\n');
            const res: ProcessLogItem = {
                text: split[0],
                class: 'running',
            };
            if (split.length > 1 && split[1].trim() !== '') {
                res.class = split[1];
            }
            return res;
        });
}

export function timeout(ms: number) {
    return new Promise((resolve) => {
        setTimeout(resolve, ms);
    });
}
