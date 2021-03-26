import {Format, Sequence} from '@/modules/reformat/types';

export const CLUSTAL: Format = {
    name: 'CLUSTAL',
    autoTransformToFormat: 'FASTA',

    validate(value: string): boolean {
        const lines = value.trimLeft().split('\n');

        if (!lines[0].startsWith('CLUSTAL')) {
            return false;
        }

        const blocks = parseBlocks(lines.slice(1));

        return blocks !== undefined
            && validateSameLengthBlocks(blocks)
            && validateHeaders(blocks)
            && validateSequences(blocks);
    },

    read(value: string): Sequence[] {
        const blocks = parseBlocks(value.trimLeft().split('\n').slice(1));

        if (!blocks) {
            return [];
        }

        const sequences: Sequence[] = [];
        for (const line of blocks[0]) {
            sequences.push({identifier: line[0], seq: line[1], description: ''});
        }

        for (const block of blocks.slice(1)) {
            for (const [index, line] of block.entries()) {
                sequences[index].seq += line[1];
            }
        }

        return sequences;
    },

    write(sequences: Sequence[]): string {
        let result = 'CLUSTAL multiple sequence alignment\n\n';

        const maxLength = Math.max(...sequences.map((val: Sequence) => val.seq.length));

        for (let j = 0; j < Math.ceil(maxLength / 60); j++) {
            for (const seq of sequences) {
                result += seq.identifier.replace(/\s/g, '');
                result += '\t';
                result += seq.seq.slice(j * 60, (j + 1) * 60);
                result += '\n';
            }
            result += '\n\n';
        }

        return result;
    },
};


/** A sequence data line is a pair consisting of the identifier and the sequence string. */
type Line = [string, string];

type Block = Line[];


/**
 * Checks whether all blocks contain the same number of data lines
 */
function validateSameLengthBlocks(blocks: Block[]): boolean {
    return blocks.every((block: Block) => block.length === blocks[0].length);
}

/**
 * Checks whether all data line headers match
 */
function validateHeaders(blocks: Block[]): boolean {
    return blocks.every((block: Block) => block.every((line: Line, i: number) =>
        line[0] === blocks[0][i][0]));
}

/**
 * Checks whether all sequences in a block are of the same length (though the length can differ between blocks)
 */
function validateSequences(blocks: Block[]): boolean {
    return blocks.every((block: Block) => block.every((line: Line) =>
        line[1].length === block[0][1].length));
}

/**
 * Parses a clustal input (without its header) into an array of blocks
 */
function parseBlocks(lines: string[]): Block[] | undefined {
    const blocks = [];

    let currentBlock = [];
    for (let i = 0; i <= lines.length; i++) {
        if (i === lines.length || isNoDataLine(lines[i])) {
            if (currentBlock.length > 0) {
                const parsed = parseBlock(currentBlock);
                if (!parsed) {
                    return undefined;
                }
                blocks.push(parsed);
                currentBlock = [];
            }
        } else {
            currentBlock.push(lines[i]);
        }
    }

    return blocks;
}

/**
 * Parses a block of sequence data lines
 * @param lines: the lines belonging to this block without the special alignment line at the bottom
 */
function parseBlock(lines: string[]): Block | undefined {
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
 * @param line: the line to be parsed
 */
function parseDataLine(line: string): Line | undefined {
    const regex = /^(?:\s*)(\S+)(?:\s+)(\S+)(?:\s*)(\d*)(?:\s*|$)/g;
    const match = regex.exec(line);
    if (!match) {
        return undefined;
    }
    return [match[1], match[2]];
}

function isNoDataLine(line: string): boolean {
    return line.trim() === '' || line.includes('*');
}
