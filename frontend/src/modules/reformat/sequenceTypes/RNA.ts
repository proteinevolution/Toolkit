import {Sequence, SequenceType} from '../types';

export const RNA: SequenceType = {
    name: 'RNA',

    validate(sequences: Sequence[]): boolean {
        return sequences.every((val: Sequence) => isRNASequence(val.seq));
    },
};

// separate function to be able to unit test this
export function isRNASequence(seq: string): boolean {
    return !/[^-AGUC]/i.test(seq);
}
