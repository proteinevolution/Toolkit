import {Format, Sequence} from '@/modules/reformat/types';

export const CLUSTAL: Format = {
    name: 'Clustal',

    validate(value: string): boolean {
        return readInternal(value) !== undefined;
    },

    read(value: string): Sequence[] {
        return readInternal(value) || [];
    },

    write(sequences: Sequence[]): string {
        return '';
    },
};

function readInternal(value: string): Sequence[] | undefined {
    const lines = value.trimLeft().split('\n');

    // check header
    if (!lines[0].startsWith('CLUSTAL')) {
        return undefined;
    }

    // parse lines
    const parsed = parseBlocks(lines.slice(1));

    // TODO convert parsed lines into sequences, check if identifiers match etc.

    return undefined;
}

export function parseBlocks(lines: string[]): Array<Array<[string, string]>> | undefined {
    const blocks: Array<Array<[string, string]>> = [];
    let currentBlock: string[] = [];

    for (const line of lines) {
        if (isNoDataLine(line)) {
            if (currentBlock.length > 0) {
                const parsed = parseBlock(currentBlock);
                if (!parsed) {
                    return undefined;
                }
                blocks.push(parsed);
                currentBlock = [];
            }
        } else {
            currentBlock.push(line);
        }
    }

    return blocks;
}

/**
 * Parses a block of sequence data lines
 *
 * @param lines: the lines of this block without the special alignment line at the bottom
 * @return An array of sequence data pairs
 */
function parseBlock(lines: string[]): Array<[string, string]> | undefined {
    const result = [];
    for (const line of lines) {
        const data = parseDataLine(line);
        if (!data) {
            return undefined;
        }
        result.push(data);
    }
    return result;
}


/**
 * Parses a single sequence data line in a block.
 *
 * @param value: the line to be parsed
 * @return A pair consisting of the identifier and the sequence string
 */
function parseDataLine(value: string): [string, string] | undefined {
    const regex = /^(?:\s*)(\S+)(?:\s+)(\S+)(?:\s*)(\d*)(?:\s*|$)/g;
    const match = regex.exec(value);
    if (!match) {
       return undefined;
    }
    return [match[1], match[2]];
}

function isNoDataLine(line: string): boolean {
    return line.trim() === '' || line.includes('*');
}
