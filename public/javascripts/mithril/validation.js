/**
 * Created by snam on 03.03.17.
 */


function validation(elem, isInit) {

    if(!isInit) {
        return $(elem).on("keyup", function (e) {

            console.log($(elem).val());

        });
    }
}