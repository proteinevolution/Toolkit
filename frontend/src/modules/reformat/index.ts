/**
 * REFORMAT.TS
 * Author: Felix Gabler
 *
 * Based upon:
 * REFORMAT.JS
 * Authors: Seung-Zin Nam, David Rau
 */
import {Format} from '@/modules/reformat/formats/Format';
import {FASTA} from '@/modules/reformat/formats/FASTA';

/**
 * Register formats here.
 */
const possibleFormats: Format[] = [FASTA];


/**
 * Validate sequences and check if they have the expected format.
 * @param seqs
 * @param expectedFormat
 */
export function validate(seqs: string, expectedFormat: string): boolean {
    return seqs !== '' && getFormat(seqs) === expectedFormat;
}

function getFormat(seqs: string): string {
    for (const format of possibleFormats) {
        if (format.validate(seqs)) {
            return format.name;
        }
    }
    return '';
}

