import {AlignmentValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentValidation} from '@/types/toolkit';

export function validation(val: string, params?: AlignmentValidation): AlignmentValidationResult {
    const elem: Reformat = new Reformat(val);
    (window as any).test = elem;

    if (val.length > 0) {
        const detectedFormat: string = elem.getFormat();
        const isFasta: boolean = elem.validate('Fasta');

        if (detectedFormat === '') {
            return result(true, 'danger', 'invalidCharacters');
        } else if (!isFasta) {
            return result(false, 'success', 'shouldAutoTransform');
        } else {
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