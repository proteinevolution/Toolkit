import {Sequence, SequenceType} from '../types';

export const DNA: SequenceType = {
    name: 'DNA',

    validate(sequences: Sequence[]): boolean {
        return sequences.every((val: Sequence) => isDNASequence(val.seq));
    },
};

// separate function to be able to unit test this
// TODO should a sequence containing only "-" be allowed here? it is allowed in the production toolkit
export function isDNASequence(seq: string): boolean {
    return !/[^-AGTC]/i.test(seq);
}
