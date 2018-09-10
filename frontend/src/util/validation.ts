import {AlignmentValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentValidation} from '@/types/toolkit';
import {AlignmentSeqType} from '@/types/toolkit/enums';

export function validation(val: string, params: AlignmentValidation): AlignmentValidationResult {
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

            if (params.maxNumSeq && !elem.maxSeqNumber(params.maxNumSeq)) {
                return result(true, 'danger', 'maxSeqNumber', {limit: params.maxNumSeq});
            }
            if (params.minNumSeq && !elem.minSeqNumber(params.minNumSeq)) {
                return result(true, 'danger', 'minSeqNumber', {limit: params.minNumSeq});
            }
            if (params.maxCharPerSeq && !elem.maxSeqLength(params.maxCharPerSeq)) {
                return result(true, 'danger', 'maxSeqLength', {limit: params.maxCharPerSeq});
            }
            if (params.minCharPerSeq && !elem.minSeqLength(params.minCharPerSeq)) {
                return result(true, 'danger', 'minSeqLength', {limit: params.minCharPerSeq});
            }
            if (elem.hasEmptyHeaders()) {
                return result(true, 'danger', 'emptyHeader');
            }
            if (elem.onlyDashes()) {
                return result(true, 'danger', 'onlyDashes');
            }
            if (!elem.maxLength(20000000)) {
                return result(true, 'danger', 'maxLength', {limit: 20000000});
            }
            if (!elem.uniqueIDs()) {
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

function result(failed: boolean, cssClass: string, textKey: string, textKeyParams?: any): AlignmentValidationResult {
    return {
        failed,
        textKey,
        cssClass,
        textKeyParams,
    };
}
