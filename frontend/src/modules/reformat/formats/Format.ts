export interface Format {
    /**
     * Name of the format.
     */
    name: string;

    /**
     * List of other formats which can be translated to this format.
     */
    translatableFormats: string[];

    /**
     * Dictionary of functions for all the other formats which can be translated to this format.
     * Each function is supplied with a string in the other format and returns a string in this format.
     */
    translate: any;

    /**
     * Validate the given input and check whether it is the correct format.
     * TODO return string to display to the user.
     * @param value
     */
    validate(value: string): boolean;
}
