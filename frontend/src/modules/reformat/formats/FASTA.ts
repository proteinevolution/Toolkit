import {Format, Sequence} from '@/modules/reformat/types';

export const FASTA: Format = {
    name: 'FASTA',

    validate(value: string): boolean {
        // checks double occurrences of ">" in the header
        // Ignore #A3M# to allow A3M format input, and ignore line breaks at the beginning as well.
        value = value.replace(/^#A3M#/, '')
            .replace(/^\n+/, '');

        if (value.startsWith('>')) {

            // if there are no '>'s at all, it is not FASTA.
            if (!value.includes('>')) {
                return false;
            }

            const sequences = value.split('\n>');

            for (let sequence of sequences) {

                // immediately remove trailing spaces
                sequence = sequence.trim();

                // check if header contains at least one char
                if (sequence.length < 1) {
                    return false;
                }

                // insert separator at the beginning again
                sequence = '>' + sequence;

                // split on newlines
                const lines = sequence.split('\n');

                // remove one line, starting at the first position
                lines.splice(0, 1);

                // join the array back into a single string without newlines
                sequence = lines.join('').trim();

                // if no sequence is found for header, it can't be FASTA.
                if (sequence === '') {
                    return false;
                }

                if (/[^-.*A-Z\s]/i.test(sequence.toUpperCase())) {
                    return false;
                }

            }

            return true;
        }

        // check if there are any headers or illegal characters at all
        // (if not it might be intended as a single-line sequence)
        return !(/[^-.*A-Z\s]/i.test(value));
    },

    read(fasta: string): Sequence[] {
        const newlines = fasta.split('\n')
        // remove empty lines
            .filter((line: string) => line === '');

        const result: Sequence[] = [];

        for (let i = 0; i < newlines.length;) {
            const element: Sequence = {
                identifier: '',
                seq: '',
            };
            if (newlines[i].startsWith('>')) {
                element.identifier = newlines[i].substring(1);
                i++;
            }
            while (i < newlines.length && !newlines[i].startsWith('>')) {
                if (!newlines[i].startsWith(';')) {
                    element.seq += newlines[i];
                }
                i++;
            }
            result.push(element);
        }
        return result;
    },

    write(sequences: Sequence[]): string {
        let result = '';
        for (const sequence of sequences) {
            result += '>';
            result += sequence.identifier;
            result += '\n';
            // result += formatLongSeq(sequence.seq, 60); TODO write formatLongSeq
            // removes stars from the end of sequences, as they are specific to the pir format
            if (/\*$/.test(sequence.seq)) {
                result = result.replace(/\*$/, '');
            }
            result += '\n';
        }

        return result;
    },
};
