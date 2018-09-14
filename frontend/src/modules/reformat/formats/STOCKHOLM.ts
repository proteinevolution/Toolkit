import {Format, Sequence} from '@/modules/reformat/types';

export const STOCKHOLM: Format = {
    name: 'Stockholm',
    autoTransformToFormat: 'FASTA',

    validate(value: string): boolean {
        return false;
    },

    read(value: string): Sequence[] {
        return [];
    },

    write(sequences: Sequence[]): string {
        return '';
    },
};
