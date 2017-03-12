/**
 * Created by snam on 03.03.17.
 */


validation = function(elem, isInit, ctx) {

    if(!isInit) {

        var toolname = $("#toolnameAccess").val();

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

                    if($(elem).reformat('alignment'))
                        console.log('alignment');
                    else console.log('not aligned');

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