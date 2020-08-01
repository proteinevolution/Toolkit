import {Sequence, SequenceType} from '../types';

export const PROTEIN: SequenceType = {
    name: 'Protein',

    validate(sequences: Sequence[]): boolean {
        return sequences.every((val: Sequence) => isProteinSequence(val.seq));
    },
};

// separate function to be able to unit test this
export function isProteinSequence(seq: string): boolean {
    return !/[^\s*A-Z.-]/i.test(seq);
}
