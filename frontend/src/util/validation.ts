import {AlignmentValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentValidation} from '@/types/toolkit';

export function validation(val: string, params?: AlignmentValidation): AlignmentValidationResult {
    const elem: Reformat = new Reformat(val);

    if (val.length > 0) {
        const detectedFormat: string = elem.getFormat();
        const isFasta: boolean = elem.validate('Fasta');

        if (detectedFormat === '') {
            return result(true, 'danger', 'invalidCharacters');
        } else if (!isFasta) {
            return result(false, 'success', 'shouldAutoTransform');
        } else {
            // valid fasta

            // Checks depending on parameters
            // todo maybe remove the null check --> make param object required
            if (params) {
                if (params.checkNucleotide && elem.isNucleotide()) {
                    return result(true, 'danger', 'nucleotideError');
                } else if (elem.hasEmptyHeaders()) {
                    return result(true, 'danger', 'emptyHeader');
                } else if (params.maxNumSeq && !elem.maxSeqNumber(params.maxNumSeq)) {
                    return result(true, 'danger', 'maxSeqNumber');
                } else if (params.minNumSeq && !elem.minSeqNumber(params.minNumSeq)) {
                    return result(true, 'danger', 'minSeqNumber');
                } else if (params.maxCharPerSeq && !elem.maxSeqLength(params.maxCharPerSeq)) {
                    return result(true, 'danger', 'maxSeqLength');
                } else if (params.minCharPerSeq && !elem.minSeqLength(params.minCharPerSeq)) {
                    return result(true, 'danger', 'minSeqLength');
                }
            }

            // Checks that are independent of parameters/tools
            if (elem.hasEmptyHeaders()) {
                return result(true, 'danger', 'emptyHeader');
            } else if (elem.onlyDashes()) {
                return result(true, 'danger', 'onlyDashes');
            } else if (!elem.maxLength(20000000)) {
                return result(true, 'danger', 'maxLength');
            } else if (!elem.uniqueIDs()) {
                return result(false, 'warning', 'uniqueIDs');
            }

            return result(false, 'success', 'proteinFasta');
        }
    }

    return result(false, '', '');
}

export function transformToFasta(val: string): string {
    return new Reformat(val).reformat('FASTA');
}

function result(failed: boolean, cssClass: string, textKey: string): AlignmentValidationResult {
    return {
        failed,
        textKey,
        cssClass,
    };
}
