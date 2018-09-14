export interface Format {
    /**
     * Name of the format.
     */
    name: string;

    /**
     * Name of the format to which inputs of this format should be auto-transformed if possible
     */
    autoTransformToFormat?: string;

    /**
     * Function which can translate array of internal sequences to this format
     * @param sequences - array of sequence strings
     */
    write(sequences: Sequence[]): string;

    /**
     * Function which can read this format to internal sequences.
     * @param value - sequences string in this format
     */
    read(value: string): Sequence[];

    /**
     * Validate the given input and check whether it is the correct format.
     * @param value
     */
    validate(value: string): boolean;
}

export interface SequenceType {
    name: string;

    /**
     * Check whether the contents of the internal sequences are of this type
     * @param sequences
     */
    validate(sequences: Sequence[]): boolean;
}

/**
 * Internal sequence object
 */
export interface Sequence {
    /**
     * Unique identifier of the sequence. Usually the first word following the '>' symbol in the header.
     */
    identifier: string;

    /**
     * Amino acid or nucleotide sequence.
     */
    seq: string;

    /**
     * Rest of the header. Can be empty.
     */
    description?: string;
}
