import {ValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentSeqType, TextAreaInputType} from '@/types/toolkit/enums';
import {
    AccessionIDValidationParams,
    RegexValidationParams,
    SequenceValidationParams,
    ValidationParams,
    PlainTextValidationParams,
} from '@/types/toolkit/tools';

export function validation(val: string, inputType: TextAreaInputType, params: ValidationParams): ValidationResult {
    switch (inputType) {
        case TextAreaInputType.SEQUENCE:
            return validateSequence(val, params as SequenceValidationParams);
        case TextAreaInputType.REGEX:
            return validateRegex(val, params as RegexValidationParams);
        case TextAreaInputType.PDB:
            return validatePDB(val);
        case TextAreaInputType.ACCESSION_ID:
            return validateAccessionID(val, params as AccessionIDValidationParams);
        case TextAreaInputType.PLAIN_TEXT:
            return validatePlainText(val, params as PlainTextValidationParams);
    }
}

function validateSequence(val: string, params: SequenceValidationParams): ValidationResult {
    const elem: Reformat = new Reformat(val);

    if (val.length > 0) {
        const detectedFormat: string = elem.getFormat();
        const autoTransformToFormat: string = elem.getAutoTransformToFormat();
        const msaDetected: boolean = elem.getNumbers() > 1;

        if (detectedFormat === '') {
            return result(true, 'danger', 'invalidCharacters');
        } else if (autoTransformToFormat && params.allowedSeqFormats.map((v) => v.toString().toUpperCase())
            .includes(autoTransformToFormat)) {
            return result(false, 'success', 'shouldAutoTransform', {
                detected: detectedFormat,
                transformFormat: autoTransformToFormat,
            });
        } else {
            // TODO order of validation checks
            if (!params.allowedSeqFormats.map((v) => v.toString().toUpperCase())
                .includes(detectedFormat.toUpperCase())) {
                return result(true, 'danger', 'invalidSequenceFormat', {expected: params.allowedSeqFormats});
            }
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
            if (params.requiresSameLengthSeq && !elem.sameLength() && detectedFormat.toLocaleUpperCase() !== 'A3M') {
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
                return result(false, 'warning', 'uniqueIDs', undefined, msaDetected);
            }
            if (params.allowedSeqType === AlignmentSeqType.PROTEIN && elem.isNucleotide()) {
                return result(false, 'warning', 'nucleotideError', undefined, msaDetected);
            }

            const typeName: string | undefined = elem.getTypes().find((type: string) =>
                type.toUpperCase() === params.allowedSeqType.toUpperCase());
            return result(false, 'success', 'valid', {type: typeName, format: detectedFormat}, msaDetected);
        }
    }

    return result(false, '', '');
}

export function transformToFormat(val: string, format: string): string {
    return new Reformat(val).reformat(format);
}

export function validateRegex(val: string, params: RegexValidationParams): ValidationResult {
    if (val.length > 0) {
        if (/\s/.test(val)) {
            return result(true, 'danger', 'invalidWhiteSpace');
        }
        if (val.length > params.maxRegexLength) {
            return result(true, 'danger', 'maxRegexLength', {limit: params.maxRegexLength});
        }
        return result(false, 'success', 'validRegex');
    }
    return result(false, '', '');
}

export function validatePDB(val: string): ValidationResult {
    if (val.length > 0) {
        let atomCounter = 0;
        const atomRecords = val.split('\n');
        for (const record of atomRecords) {
            if (/^ATOM/.test(record)) {
                atomCounter++;
                if (atomCounter === 21) {
                    return result(false, 'success', 'validPDB');
                }
            }
        }
    }
    return result(true, 'danger', 'invalidPDB');
}


export function validateAccessionID(val: string, params: AccessionIDValidationParams): ValidationResult {
    if (val.replace(/\s/g, '') === '') {
        return result(true, 'danger', 'invalidAccessionID');
    }
    if (val.split(/[\r\n]+/).length > params.maxNumIDs) {
        return result(true, 'danger', 'maxIDNumber', {limit: params.maxNumIDs});
    }
    return result(false, 'success', 'validAccessionID');
}

export function validatePlainText(val: string, params: PlainTextValidationParams): ValidationResult {
    if (val.replace(/\s/g, '') === '') {
        return result(true, 'danger', 'invalidAccessionID'); // TODO key
    }
    if (val.length > params.maxNumChars) {
        return result(true, 'danger', 'maxLength', {limit: params.maxNumChars}); // TODO key
    }
    return result(false, 'success', 'validAccessionID'); // TODO key
}

function result(failed: boolean, cssClass: string, textKey: string,
                textKeyParams?: any, msaDetected?: boolean): ValidationResult {
    return {
        failed,
        cssClass,
        textKey,
        textKeyParams,
        msaDetected,
    };
}
