import {Format, Sequence} from '@/modules/reformat/types';

export const STOCKHOLM: Format = {
    name: 'Stockholm',

    validate(value: string): boolean {
        return true;
    },

    read(value: string): Sequence[] {
        return [];
    },

    write(sequences: Sequence[]): string {
        return '';
    },
};
