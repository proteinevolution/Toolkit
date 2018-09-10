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
            // TODO order of validation checks
            // TODO auto transform or refuse disallowed formats?
            if (!elem.isOfType(params.allowedSeqType)) {
                return result(true, 'danger', 'invalidSequenceType', {expected: params.allowedSeqType});
            }
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
            if (params.allowedSeqType === AlignmentSeqType.PROTEIN && elem.isNucleotide()) {
                return result(false, 'warning', 'nucleotideError');
            }

            const typeName: string = elem.getTypes().filter((type: string) =>
                type.toUpperCase() === params.allowedSeqType.toUpperCase())[0];
            return result(false, 'success', 'valid', {type: typeName, format: detectedFormat});
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
