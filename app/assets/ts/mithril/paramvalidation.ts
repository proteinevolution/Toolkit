/// <reference path="validation.ts"/>


let paramValidation = function(elem : any, isInit : boolean, ctx : any) : any {

    if(!isInit) {

        let toolname : string;
        try { toolname = $("#toolnameAccess").val(); }
        catch(err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        return $(elem).on("keyup mouseover", function (e) {
            setTimeout(function () {
                e.preventDefault();
                switch (toolname) {
                    case "modeller":
                        if($(elem).attr('id') == "regkey" && $(elem).val().length > 0) {

                            m.request({
                                method: "GET",
                                url: "/validate/modeller/" + $(elem).val(),
                                background: true
                            }).then(function (result) {
                                console.log($(elem).val());
                                if (JSON.stringify(result) == "\"valid\"") {
                                    $( "#regkey" ).css("background-color", "rgb(219, 255, 219)");
                                    if(modellerIsValid) {
                                        $(".submitJob").prop("disabled", false);
                                    }
                                }
                                else {
                                    $( "#regkey" ).css("background-color", "rgb(255, 221, 221)");
                                    $(".submitJob").prop("disabled", true);
                                }

                            }).catch(function(e) {
                                console.warn(e);
                            })
                        }
                        break;
                    case "samcc":
                        if(samccIsValid) {
                            let counter = 0;
                            if ($("#samcc_helixone").val() == "a")
                                counter++;
                            if ($("#samcc_helixtwo").val() == "a")
                                counter++;
                            if ($("#samcc_helixthree").val() == "a")
                                counter++;
                            if ($("#samcc_helixfour").val() == "a")
                                counter++;
                            if (counter == 4)
                                $(".submitJob").prop("disabled", false);
                            else $(".submitJob").prop("disabled", true);
                        }
                        break;
                    default:
                        break;
                }
            }, 500)

            })

    }
};