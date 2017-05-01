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
            e.preventDefault();
            switch (toolname) {
                case "modeller":
                    if($(elem).attr('id') == "regkey" && $(elem).val().length > 0) {

                        m.request({
                            method: "GET",
                            url: "/validate/modeller/" + $(elem).val(),
                            background: true
                        }).then(function (result) {
                                if(modellerIsValid) {
                                    if (JSON.stringify(result) == "\"valid\"") {
                                        $( "#regkey" ).css("background-color", "#DBFFDB");
                                        $(".submitJob").prop("disabled", false);
                                    }
                                    else {
                                        $( "#regkey" ).css("background-color", "#FFDDDD");
                                        $(".submitJob").prop("disabled", true);
                                    }
                                }
                                else {
                                        $(".submitJob").prop("disabled", true);
                                    }

                        }).catch(function(e) {
                            console.warn(e);
                        })
                    }
                    break;
                default:
                    break;
            }
        })
    }
};