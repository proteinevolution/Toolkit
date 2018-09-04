/**
 * REFORMAT.TS
 * Author: Felix Gabler
 *
 * Based upon:
 * REFORMAT.JS
 * Authors: Seung-Zin Nam, David Rau
 */
import {Format, Operation, Sequence} from '@/modules/reformat/types';
import {FASTA} from '@/modules/reformat/formats/FASTA';
import {detect, numbers, uniqueIDs, sameLength} from '@/modules/reformat/operations';
import {CLUSTAL} from '@/modules/reformat/formats/CLUSTAL';
import {A3M} from '@/modules/reformat/formats/A3M';
import {STOCKHOLM} from '@/modules/reformat/formats/STOCKHOLM';

/**
 * Register possible formats here.
 */
const supportedFormats: Format[] = [
    FASTA,
    CLUSTAL,
    A3M,
    STOCKHOLM,
];

/**
 * Register possible operations here.
 */
const supportedOperations: Operation[] = [
    numbers,
    detect,
    sameLength,
    uniqueIDs,
];

/**
 * Validate sequences and check if they have the expected format.
 * @param seqs
 * @param expectedFormat
 */
export function validate(seqs: string, expectedFormat: string): boolean {
    const format: Format | null = getFormat(seqs);
    return format !== null && format.name.toUpperCase() === expectedFormat.toUpperCase();
}

export function reformat(seqs: string, operation: string, ...params: any[]): string | boolean | number {
    const format: Format | null = getFormat(seqs);
    // format will also be null if seqs are empty string
    if (format === null) {
        return false;
    }

    operation = operation.toUpperCase();
    const sequences: Sequence[] = format.read(seqs);

    // check if operation is reformatting to another format
    for (const targetFormat of supportedFormats) {
        if (targetFormat.name.toUpperCase() === operation) {
            return targetFormat.write(sequences);
        }
    }

    // check if operation is any of the supported operations
    for (const op of supportedOperations) {
        if (op.name.toUpperCase() === operation) {
            return op.execute(sequences, seqs, format, params);
        }
    }

    return false;
}

function getFormat(seqs: string): Format | null {
    if (seqs === '') {
        return null;
    }
    for (const format of supportedFormats) {
        if (format.validate(seqs)) {
            return format;
        }
    }
    return null;
}

