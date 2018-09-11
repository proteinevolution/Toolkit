import {Format, Sequence} from '@/modules/reformat/types';
import {FASTA} from './FASTA';

export const A3M: Format = {
    name: 'A3M',

    validate(value: string): boolean {
        // remove preceding spaces and newlines
        value = value.trimLeft();

        // Has to start with #A3M# and then be valid FASTA
        if (value.startsWith('#A3M#')) {
            return FASTA.validate(value.slice(5));
        }
        return false;
    },

    read(a3m: string): Sequence[] {
        return FASTA.read(a3m.trimLeft().slice(5));
    },

    write(sequences: Sequence[]): string {
        return '#A3M#\n' + FASTA.write(sequences);
    },
};
