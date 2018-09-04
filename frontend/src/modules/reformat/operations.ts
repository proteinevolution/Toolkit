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

export const sameLength: Operation = {
    name: 'sameLength',
    execute(sequences: Sequence[]): boolean {
        const firstLength = sequences[0].seq.length;
        return sequences.every((val: Sequence) => val.seq.length === firstLength);
    },
};

export const minSeqNumber: Operation = {
    name: 'minSeqNumber',
    execute(sequences: Sequence[], seq, format, minSeqLimit: number): boolean {
        return sequences.length >= minSeqLimit;
    },
};

export const maxSeqNumber: Operation = {
    name: 'maxSeqNumber',
    execute(sequences: Sequence[], seq, format, maxSeqLimit: number): boolean {
        return sequences.length <= maxSeqLimit;
    },
};


export const minSeqLength: Operation = {
    name: 'minSeqLength',
    execute(sequences: Sequence[], seq, format, minCharPerSeq: number): boolean {
        return sequences.every((val) => val.seq.length >= minCharPerSeq);
    },
};

export const maxSeqLength: Operation = {
    name: 'maxSeqLength',
    execute(sequences: Sequence[], seq, format, maxCharPerSeq: number): boolean {
        return sequences.every((val) => val.seq.length <= maxCharPerSeq);
    },
};


export const uniqueIDs: Operation = {
    name: 'uniqueIDs',
    execute(sequences: Sequence[]): boolean {
        const uniqueIdentifiers = new Set(sequences.map((val) => val.identifier));
        return uniqueIdentifiers.size === sequences.length;
    },
};


export const onlyDashes: Operation = {
    name: 'onlyDashes',
    execute(sequences: Sequence[]): boolean {
        // TODO
        return false;
    },
};
