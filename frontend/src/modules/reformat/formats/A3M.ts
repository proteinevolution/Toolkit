import {Format, Sequence} from '@/modules/reformat/types';

export const A3M: Format = {
    name: 'A3M',

    validate(value: string): boolean {
        return false;
    },

    read(a3m: string): Sequence[] {
        return [];
    },

    write(sequences: Sequence[]): string {
        return '';
    },
};
