export interface ValidationResult {
    failed: boolean;
    textKey: string; // this has to be a key defined in i18n files for translation
    textKeyParams?: any;
}

export interface AlignmentValidationResult extends ValidationResult {
    cssClass: string;
}
