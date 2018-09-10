export interface ValidationResult {
    failed: boolean;
    cssClass: string;
    textKey: string; // this has to be a key defined in i18n files for translation
    textKeyParams?: any; // this should be an object containing the i18n named parameters
}
