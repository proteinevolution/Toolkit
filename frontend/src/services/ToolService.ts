import {Tool} from '@/types/toolkit';

export default class ToolService {
    public static async fetchAll() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const t1: Tool = {
                    name: 'searchtool1',
                    longname: 'Search Tool 1',
                    title: 'Great tool 1',
                    section: 'Search',
                };
                const t2: Tool = {
                    name: 'searchtool2',
                    longname: 'Search Tool 2',
                    section: 'Search',
                };

                const t3: Tool = {
                    name: 'alignmenttool1',
                    longname: 'Alignment Tool 1',
                    title: 'Great tool 2',
                    section: 'Alignment',
                };
                const t4: Tool = {
                    name: 'alignmenttool2',
                    longname: 'Alignment Tool 2',
                    section: 'Alignment',
                };
                resolve([
                    t1, t2, t3, t4,
                ]);
            }, 0);
        });
    }
}
