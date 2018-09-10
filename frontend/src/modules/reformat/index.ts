/**
 * REFORMAT.TS
 * Author: Felix Gabler, Sebastian Till
 *
 * Based upon:
 * REFORMAT.JS
 * Authors: Seung-Zin Nam, David Rau
 */
import {Format, Sequence, SequenceType} from '@/modules/reformat/types';
import {FASTA} from '@/modules/reformat/formats/FASTA';
import {CLUSTAL} from '@/modules/reformat/formats/CLUSTAL';
import {A3M} from '@/modules/reformat/formats/A3M';
import {STOCKHOLM} from '@/modules/reformat/formats/STOCKHOLM';
import {PROTEIN} from './sequenceTypes/PROTEIN';
import {DNA} from './sequenceTypes/DNA';
import {RNA} from './sequenceTypes/RNA';

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
 * Register sequence types here.
 */
const supportedTypes: SequenceType[] = [
    PROTEIN,
    DNA,
    RNA,
];

export class Reformat {
    private readonly seqs: string;
    private readonly sequences?: Sequence[];
    private readonly format?: Format;
    // type is stored as an array of all valid types, as multiple types could be valid at the same time
    // (e.g. protein and DNA)
    private readonly types?: SequenceType[];

    constructor(seqs: string) {
        this.seqs = seqs;
        this.format = getFormat(seqs);
        if (this.format != null) {
            this.sequences = this.format.read(seqs);
            this.types = getAllTypes(this.sequences);
        }
    }

    public getFormat(): string {
        if (this.format) {
            return this.format.name;
        }
        return '';
    }

    public getTypes(): string[] {
        return this.types ? this.types.map((val: SequenceType) => val.name) : [];
    }

    public isOfType(type: string): boolean {
        return this.types ? this.types.some((val: SequenceType) =>
            val.name.toUpperCase() === type.toUpperCase()) : false;
    }

    public isNucleotide(): boolean {
        return this.types ? this.types.includes(DNA) || this.types.includes(RNA) : false;
    }

    /**
     * Validate this.sequences and check if they have the expected format.
     * @param expectedFormat
     */
    public validate(expectedFormat: string): boolean {
        return this.format != null && this.format.name.toUpperCase() === expectedFormat.toUpperCase();
    }

    /**
     * Reformat seqs to another format.
     * @param targetFormat
     */
    public reformat(targetFormat: string): string {
        // format will also be null if seqs are empty string
        if (!this.format) {
            return '';
        }

        targetFormat = targetFormat.toUpperCase();
        const sequences: Sequence[] = this.format.read(this.seqs);

        for (const format of supportedFormats) {
            if (format.name.toUpperCase() === targetFormat) {
                return format.write(sequences);
            }
        }

        return '';
    }


    // Operations

    public getNumbers(): number {
        return this.sequences ? this.sequences.length : 0;
    }

    public hasEmptyHeaders(): boolean {
        // we cannot make the trivial check here, since a single sequence without an empty should pass the test,
        // but if a header was opened up (">"), an error should be thrown
        return false;
    }

    public sameLength(): boolean {
        if (this.sequences) {
            const firstLength = this.sequences[0].seq.length;
            return this.sequences.every((val: Sequence) => val.seq.length === firstLength);
        }
        return false;
    }

    public maxLength(charLimit: number) {
        return this.seqs.length < charLimit;
    }

    public minSeqNumber(minSeqLimit: number): boolean {
        return this.sequences ? this.sequences.length >= minSeqLimit : false;
    }

    public maxSeqNumber(maxSeqLimit: number): boolean {
        return this.sequences ? this.sequences.length <= maxSeqLimit : false;
    }


    public minSeqLength(minCharPerSeq: number): boolean {
        return this.sequences ? this.sequences.every((val: Sequence) => val.seq.length >= minCharPerSeq) : false;
    }

    public maxSeqLength(maxCharPerSeq: number): boolean {
        return this.sequences ? this.sequences.every((val: Sequence) => val.seq.length <= maxCharPerSeq) : false;
    }

    public uniqueIDs(): boolean {
        if (this.sequences) {
            const uniqueIdentifiers = new Set(this.sequences.map((val: Sequence) => val.identifier));
            return uniqueIdentifiers.size === this.sequences.length;
        }
        return false;
    }

    public onlyDashes(): boolean {
        return this.sequences ? this.sequences.every((val: Sequence) => /^[.\-]*$/.test(val.seq)) : false;
    }
}

function getFormat(seqs: string): Format | undefined {
    if (seqs === '') {
        return undefined;
    }
    for (const format of supportedFormats) {
        if (format.validate(seqs)) {
            return format;
        }
    }
    return undefined;
}

function getAllTypes(sequences: Sequence[]): SequenceType[] | undefined {
    return supportedTypes.filter((val: SequenceType) => val.validate(sequences));
}

export function formatLongSeq(seq: string): string {
    const split = seq.match(/.{1,60}/g);
    if (split === null) {
        return '';
    }
    return split.reduce((prev: string, currentValue: string, currentIndex: number) =>
        prev + currentValue + (currentIndex < split.length - 1 ? '\n' : ''), '');
}
