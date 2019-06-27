import {Format, Sequence} from '@/modules/reformat/types';

export const STOCKHOLM: Format = {
    name: 'STOCKHOLM',
    autoTransformToFormat: 'FASTA',

    validate(value: string): boolean {
        if (!value) {
            return false;
        }

        const lines = value.trimLeft().split('\n').filter(Boolean);

        if (lines.length === 0 || !lines[0].startsWith('# STOCKHOLM 1.0')) {
            return false;
        }

        for (const line of lines) {
            if (line.startsWith('//')) {
                break;
            }

            if (!line.startsWith('#')) {
                const split = line.split(/\s/g).filter(Boolean);
                if (split.length < 2) {
                    // Sequence or sequence name invalid
                    return false;
                }
                if (/[^\-\\.*A-Z\s]/i.test(split[1])) {
                    // Alignment contains invalid symbols
                    return false;
                }
            }
        }

        return true;
    },

    read(value: string): Sequence[] {
        const result: Sequence[] = [];

        const lines = value.trimLeft().split('\n').filter(Boolean).slice(1);

        for (const line of lines) {
            if (line.startsWith('//')) {
                break;
            }

            if (!line.startsWith('#')) {
                const element: Sequence = {
                    identifier: '',
                    seq: '',
                };
                const split = line.split(/\s/g).filter(Boolean);
                element.identifier = split[0];
                element.seq = split[1];
                result.push(element);
            }
        }

        return result;
    },

    write(sequences: Sequence[]): string {
        let result = '';
        result += '# STOCKHOLM 1.0';
        result += '\n';
        result += '#GF SQ ' + sequences.length;
        result += '\n';
        for (const sequence of sequences) {
            result += '#GF ' + sequence.identifier.replace(/\s/g, '') + ' DE ' + sequence.identifier;
            result += '\n';
            result += sequence.identifier.replace(/\s/g, '');
            result += ' \t';
            result += sequence.seq;
            result += '\n';
        }

        return result;
    },
};

