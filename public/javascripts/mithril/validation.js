/**
 * Created by snam on 03.03.17.
 */


validation = function(elem, isInit, ctx) {

    if(!isInit) {

        /** hack: get toolname from the URL because you cannot pass it as a variable to a config properly */

        var url = window.location.href;
        var parts = url.split("/");
        var toolname = parts[parts.length-1];

        return $(elem).on("keyup", function (e) {

            switch(toolname) {
                case "tcoffee":
                    /** validation model for tcoffee:
                     * input has to be FASTA
                     */
                    if(!$(elem).validate('fasta'))
                        feedback(false, "this is no fasta!");
                    else
                        feedback(true, "valid fasta");

                    break;
                case "mafft":
                    /** validation model for mafft:
                     * input has to be FASTA
                     */
                    console.log("test");
                    break;
                default:
                    console.warn("no tool specified");
            }

            //console.log($(elem).val() + toolname);

        });
    }
};


function feedback(valid, msg) {

    if(!valid) {
        console.log(msg);
        $(".submitJob").prop("disabled", true);
    } else
        $(".submitJob").prop("disabled", false);


}