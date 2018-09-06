import {AlignmentValidationResult} from '@/types/toolkit/validation';
import {Reformat} from '@/modules/reformat';
import {AlignmentValidation} from '@/types/toolkit';

export function validation(val: string, params?: AlignmentValidation): AlignmentValidationResult {
    let text: string = '';
    let cssClass: string = '';
    let failed: boolean = false;

    const elem: Reformat = new Reformat(val);
    (window as any).test = elem;

    if (val.length > 0) {
        const detectedFormat: string = elem.getFormat();
        const isFasta: boolean = elem.validate('Fasta');

        if (detectedFormat === '') {
            failed = true;
            cssClass = 'danger';
            text = 'Invalid characters. Could not detect format.';
        } else if (!isFasta) {
            failed = false;
            cssClass = 'success';
            text = `${detectedFormat} format found: Auto-transformed to FASTA`;
            // TODO: Auto-transform
            // this.text = elem.reformat('Fasta');
        } else {
            cssClass = 'success';
            text = 'Protein FASTA';
        }
    }

    return {
        failed,
        text,
        cssClass,
    };
}
