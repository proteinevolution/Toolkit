import {Format, Sequence} from '@/modules/reformat/types';
import {formatLongSeq} from '@/modules/reformat/utils';

export const PIR: Format = {
    name: 'PIR',

    validate(value: string): boolean {
        if (!value) {
            return false;
        }

        // remove preceding spaces and newlines
        value = value.trimLeft();

        // the first real character needs to be a '>'
        if (!value.startsWith('>')) {
            return false;
        }

        const sequences = value.substr(1).split('\n>');

        for (let sequence of sequences) {
            // remove all spaces
            sequence = sequence.replace(/ /g, '');

            const lines = sequence.split('\n').filter(Boolean);

            // sequence must consist of header, description and sequence
            if (lines.length < 3) {
                return false;
            }

            // validate header
            const header: string = lines[0];
            if (header.charAt(2) !== ';') {
                return false;
            }

            // validate sequence
            let seq: string = lines.slice(2).join('');

            if (!seq.endsWith('*')) {
                return false;
            }

            seq = seq.replace(/\s/g, '');

            // Check if sequences contain invalid characters
            if (/[^-.*A-Z]/i.test(seq.toUpperCase())) {
                return false;
            }
        }

        return true;
    },

    read(value: string): Sequence[] {
        const result: Sequence[] = [];

        const lines = value.trimLeft().split('\n').filter(Boolean);

        for (let i = 0; i < lines.length;) {
            const element: Sequence = {
                identifier: '',
                description: '',
                seq: '',
            };
            if (lines[i].startsWith('>')) {
                element.identifier = lines[i].substring(1);
                i++;
                element.description = lines[i];
                i++;
            }
            while (i < lines.length && !lines[i].startsWith('>')) {
                if (!lines[i].startsWith(';')) {
                    element.seq += lines[i];
                }
                i++;
            }
            // remove trailing star
            if (element.seq.endsWith('*')) {
                element.seq = element.seq.slice(0, -1);
            }
            result.push(element);
        }
        return result;
    },

    write(sequences: Sequence[]): string {
        let result = '';
        for (const sequence of sequences) {
            result += '>XX;';
            result += sequence.identifier;
            result += '\n';
            if (sequence.description) {
                result += sequence.description;
                result += '\n';
            } else {
                result += 'No description.';
                result += '\n';
            }
            result += formatLongSeq(sequence.seq);
            result += '*';
            result += '\n';
        }

        return result;
    },
};


