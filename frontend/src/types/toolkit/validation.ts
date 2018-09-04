export interface ValidationResult {
    failed: boolean;
    text: string;
}

export interface AlignmentValidationResult extends ValidationResult {
    cssClass: string;
}
