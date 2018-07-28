/// <reference path="validation.ts"/>

const paramValidation = function(elem: any, isInit: boolean, ctx: any): any {

    const helixInputIds = ["#samcc_helixone", "#samcc_helixtwo", "#samcc_helixthree", "#samcc_helixfour"];

    function validateHelix(helix: string): boolean {
        return (/^[a-r];[a-zA-Z0-9];\d+;\d+$/g.test(helix));
    }

    function validateHelixInput(helixInput: JQuery): boolean {
        const helixValid: boolean = validateHelix(helixInput.val());
        helixInput.css("background-color", helixValid ? "rgb(219, 255, 219)" : "rgb(255, 221, 221)");
        manageSubmitBtnDisabled(helixInput, helixValid);
        return helixValid;
    }

    const validate = function(debounceTime: number, toolname: string) {
        return debounce(function(e: Event) {
            e.preventDefault();
            if (toolname === "samcc") {
                let counter = 0;
                if ($("#fileUpload").val() !== "" && samccIsValid) {
                    $(".submitJob").prop("disabled", false);
                }

                for (let helixInputId of helixInputIds) {
                    if (validateHelixInput($(helixInputId))) {
                        counter++;
                    }
                }

                if (counter == 4 && samccIsValid) {
                    $(".submitJob").prop("disabled", false);
                } else {
                    $(".submitJob").prop("disabled", true);
                }
            }
        }, debounceTime);
    };

    if (!isInit) {

        let toolname: string;
        try {
            toolname = $("#toolnameAccess").val();
        }
        catch (err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        return $(elem).on("input click", validate(300, toolname));
    }
};