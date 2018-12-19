import {ValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentSeqType, TextAreaInputType} from '@/types/toolkit/enums';
import {SequenceValidationParams, ValidationParams} from '@/types/toolkit/tools';

export function validation(val: string, inputType: TextAreaInputType, params: ValidationParams): ValidationResult {
    switch (inputType) {
        case TextAreaInputType.Sequence:
            return validateSequence(val, params as SequenceValidationParams);
        case TextAreaInputType.Regex:
            return validateRegex(val);
        case TextAreaInputType.PDB:
            return validatePDB(val);
        case TextAreaInputType.AccessionID:
            return validateAccessionID(val);
    }
}

function validateSequence(val: string, params: SequenceValidationParams): ValidationResult {
    const elem: Reformat = new Reformat(val);

    if (val.length > 0) {
        const detectedFormat: string = elem.getFormat();
        const autoTransformToFormat: string = elem.getAutoTransformToFormat();

        if (detectedFormat === '') {
            return result(true, 'danger', 'invalidCharacters');
        } else if (autoTransformToFormat) {
            return result(false, 'success', 'shouldAutoTransform', {
                detected: detectedFormat,
                transformFormat: autoTransformToFormat,
            });
        } else {
            // TODO order of validation checks
            // TODO auto transform or refuse disallowed formats?
            if (!elem.isOfType(params.allowedSeqType)) {
                return result(true, 'danger', 'invalidSequenceType', {expected: params.allowedSeqType});
            }
            if (!params.allowEmptySeq && elem.hasEmptySequences()) {
                return result(true, 'danger', 'emptySequences');
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
            if (params.requiresSameLengthSeq && !elem.sameLength()) {
                return result(true, 'danger', 'sameLength');
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

            const typeName: string | undefined = elem.getTypes().find((type: string) =>
                type.toUpperCase() === params.allowedSeqType.toUpperCase());
            return result(false, 'success', 'valid', {type: typeName, format: detectedFormat});
        }
    }

    return result(false, '', '');
}

export function transformToFormat(val: string, format: string): string {
    return new Reformat(val).reformat(format);
}

export function validateRegex(val: string): ValidationResult {
    // TODO regex validation
    return result(false, 'success', 'validRegex');
}

export function validatePDB(val: string): ValidationResult {
    // TODO pdb validation
    return result(false, 'success', 'validPDB');
}

export function validateAccessionID(val: string): ValidationResult {
    // TODO accession id validation
    return result(false, 'success', 'validAccessionID');
}

function result(failed: boolean, cssClass: string, textKey: string, textKeyParams?: any): ValidationResult {
    return {
        failed,
        cssClass,
        textKey,
        textKeyParams,
    };
}
