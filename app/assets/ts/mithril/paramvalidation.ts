/// <reference path="validation.ts"/>




let paramValidation = function(elem : any, isInit : boolean, ctx : any) : any {

    function helixValid(helix:string) : boolean{
        return (/[a-g];[a-zA-Z0-9];\d+;\d+/g.test(helix))
    }

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
                            let counter = 0;

                            if (helixValid($("#samcc_helixone").val())) {
                                $("#samcc_helixone").css("background-color", "rgb(219, 255, 219)");
                                counter++;
                            }
                            else $( "#samcc_helixone" ).css("background-color", "rgb(255, 221, 221)");


                            if (helixValid($("#samcc_helixtwo").val())) {
                                $("#samcc_helixtwo").css("background-color", "rgb(219, 255, 219)");
                                counter++;
                            }
                            else $( "#samcc_helixtwo" ).css("background-color", "rgb(255, 221, 221)");

                            if (helixValid($("#samcc_helixthree").val())) {
                                $("#samcc_helixthree").css("background-color", "rgb(219, 255, 219)");
                                counter++;
                                }
                            else $( "#samcc_helixthree" ).css("background-color", "rgb(255, 221, 221)");

                            if (helixValid($("#samcc_helixfour").val())) {
                                $("#samcc_helixfour").css("background-color", "rgb(219, 255, 219)");
                                counter++;
                            }
                             else $( "#samcc_helixfour" ).css("background-color", "rgb(255, 221, 221)");


                            if (counter == 4 && samccIsValid)
                                $(".submitJob").prop("disabled", false);
                            else $(".submitJob").prop("disabled", true);
                        break;
                    default:
                        break;
                }
            }, 500)

            })
    }
};