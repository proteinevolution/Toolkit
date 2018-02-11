// input textarea

const ParameterAlignmentComponent = {
    placeholder: "",
    model: function(args : any) {
        this.modes = args.param.paramType.modes;
        this.label = "";
        ParameterAlignmentComponent.placeholder = args.param.paramType.placeholder;
        this.formats = [];
        if(this.modes.length > 0) {
            this.label = this.modes[0].label;
            if(this.modes[0].mode == 1) {
                this.formats = this.modes[0].formats
            }
        }
        this.allowsTwoTextAreas = args.param.paramType.allowsTwoTextAreas;
        this.twoTextAreas = (window.JobModel.getParamValue("hhpred_align") == 'true');
    },
    controller: function(args : any) {
        this.mo = new (<any>window).ParameterAlignmentComponent.model(args);
        return {
            name: "alignment",
            id: "alignment",
            // Function to List all supported modes of the component
            getModes: (function() {
                return this.modes;
            }).bind(this.mo),
            getLabel: (function() {
                //return this.label;
                return "Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL format."
            }).bind(this.mo),
            getAllowsTwoTextAreas: (function() {
                return this.allowsTwoTextAreas;
            }).bind(this.mo),
            setMode: (function(mode : any) {
                for(let i = 0; i < this.modes.length; i++) {
                    let current_mode = this.modes[i];
                    if(current_mode.mode == mode) {
                        this.label = current_mode.label;
                        if(mode == 1) {
                            this.formats = current_mode.formats;
                        } else {
                            this.formats = [];
                        }
                    }
                }
            }).bind(this.mo),
            getFormats: (function() {
                return this.formats;
            }).bind(this.mo),
            toggleTwoTextAreas: (function() {
                this.twoTextAreas = !this.twoTextAreas;
                if (this.twoTextAreas) {
                    $(".inputDBs").prop("disabled", true).val(null).trigger("change");
                    $(".inputDBs option:selected").prop("selected", false);
                    $("#alignment").attr("rows", "8");
                    $("#alignment_two").show().prop("required", true);
                    $("#hhpred_align").prop('checked', true);
                } else {
                    $(".inputDBs").prop('disabled', false);
                    $("#alignment").attr("rows", "14");
                    $("#alignment_two").hide().removeAttr("required", false);
                    $("#hhpred_align").prop('checked', false);

                }
            }).bind(this.mo),
            setTwoTextAreas: (function(bool : boolean) {
                this.twoTextAreas = bool;
            }).bind(this.mo),
            getTwoTextAreas: (function(){
                return this.twoTextAreas;
            }).bind(this.mo)

        };
    },
    view: function(ctrl : any, args : any) {
        const params = {
            // TODO Jquery stuff should be handled in config
            oninit: function (elem : any, isInit : boolean) {
                if (!isInit) {
                    if (ctrl.getTwoTextAreas()) {
                        $(".inputDBs").val(null).trigger("change");
                        $(".inputDBs").prop('disabled', true);
                        $(".inputDBs option:selected").prop("selected", false);
                        $("#hhpred_align").prop('checked', true);
                        $("#alignment").attr("rows", "8");
                        $("#alignment_two").show().prop("required", true);
                    } else {
                        $(".inputDBs").prop('disabled', false);
                        $("#hhpred_align").prop('checked', false);
                        $("#alignment").attr("rows", "14");
                        $("#alignment_two").hide().removeAttr("required", false);

                    }
                }
            }
        };
        let alignmentSwitch, textArea2;
        if(ctrl.getAllowsTwoTextAreas()) {

            alignmentSwitch = m("div", {"class": "switchContainer"},
                m("label", {"class": "switch tiny"},
                    m("input", {
                        id: "hhpred_align",
                        type: "checkbox",
                        name: "hhpred_align",
                        value: "true",
                        config: params.oninit,
                        onclick: function () {
                            ctrl.toggleTwoTextAreas();
                        }}),
                    m("div", {"class": "sliderSwitch round"})
                ),
                m("label",{"class": "firstLabel"},"Align two sequences or MSAs")
            );
            textArea2 =
                m("textarea", {
                    name: ctrl.name+"_two",
                    placeholder: ctrl.getLabel(),
                    title: "",
                    rows: 8,
                    cols: 70,
                    "class": "alignment",
                    id: ctrl.id + "_two",
                    value: window.JobModel.getParamValue("alignment_two"),
                    style: "display: none; margin-top: 1em;",
                    spellcheck: false,
                    config: validation
                });
        }

        return renderParameter([
            m("div", {
                    "class": "alignment_textarea"
                },
                m("textarea", {
                    name: ctrl.name,
                    placeholder: ParameterAlignmentComponent.placeholder,
                    title: "",
                    rows: 14,
                    cols: 70,
                    id: ctrl.id,
                    "class": "alignment",
                    value: args.value,
                    required: "required",
                    spellcheck: false,
                    config: validation
                }),
                textArea2)
            , m("div", {
                "class": "alignment_buttons"
            }, [
                m("div", {"class": "leftAlignmentButtons"},
                    m("input", {
                        type: "button",
                        id: "pasteButton",
                        "class": "button small alignmentExample",
                        value: "Paste Example",
                        config: sampleSeqConfig,
                        onclick: function() {
                            setTimeout(function(){
                                validationProcess($('#alignment'),$("#toolnameAccess").val());
                            }, 100);
                            $("#validOrNot").removeClass("alert warning primary secondary");
                            originIsFasta = true; // resets changed validation filter
                        }
                    }),
                    m("div", {"class": "uploadContainer"},
                        m("label",{
                            "for": "fileUpload",
                            "class" : "button small fileUpload"
                        },"Upload File"),
                        m("input", {
                            type: "file",
                            id: "fileUpload",
                            "class": "show-for-sr",
                            onchange: function() {
                                if (this.value) {
                                    $(".uploadFileName").show();
				                    $("#uploadBoxClose").show();
                                    $("#" + ctrl.id).prop("disabled", true);
                                    $("#" + ctrl.id + "_two").prop("disabled", true);
				                    validationProcess($('#alignment'),$("#toolnameAccess").val());
                                }
                            }
                        }), m("div",
                            {"class": "uploadFileName"},
                            $("#fileUpload").val() ? $("#fileUpload").val().substring($("#fileUpload").val().lastIndexOf("\\") +1) : "",
                            m("a", {
                                "class": "boxclose",
                                "id": "uploadBoxClose",
                                onclick: function(){
                                    $(".uploadFileName").hide();
                                    $("input[type=file]").val(null);
                                    validationProcess($('#alignment'),$("#toolnameAccess").val());
			            return $("#" + ctrl.id).prop("disabled", false);
                                }
                            }, m("i", {"class": "fa fa-trash-o"})))),
                    m(JobValidationComponent, {})
                    , m("select", {"id": "alignment_format", "class": "alignment_format", config: alignment_format.bind(ctrl.getFormats())}, ctrl.getFormats().map(function(format : any){
                            return m("option", {value: format[0]}, format[1])}
                        )
                    ),
                    m("div", {"class": "switchDiv"},
                        alignmentSwitch
                    ))
            ])
        ], "alignmentParameter");
    }
};
