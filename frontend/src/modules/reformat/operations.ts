import {Format, Operation, Sequence} from '@/modules/reformat/types';

export const numbers: Operation = {
    name: 'numbers',
    execute(sequences: Sequence[]): number {
        return sequences.length;
    },
};


export const detect: Operation = {
    name: 'detect',
    execute(sequences: Sequence[], seqs: string, format: Format): string {
        return format.name;
    },
};
