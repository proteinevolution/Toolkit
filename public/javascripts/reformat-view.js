var myCodeMirror = CodeMirror(document.getElementById("inputMirror"), {
    //value: inputClustal,
    lineNumbers: false,
    mode: "none",
    theme: "reformat"
});
myCodeMirror.setSize("100%", 220);
$("#reformatExample").click(function () {
    myCodeMirror.setValue(inputClustal);
});

var myCodeMirror2 = CodeMirror(document.getElementById("outputMirror"), {
    value: "",
    lineNumbers: false,
    readOnly: true,
    mode: "none",
    theme: "reformat"
});

myCodeMirror.setSize("100%", 220);
myCodeMirror2.setSize("100%", 220);

var resultCookie = GeneralTabComponent.controller().forwardString();
if (resultCookie) {
    myCodeMirror.setValue(resultCookie);
}

function forwardTo(tool) {
    if (myCodeMirror2.lineCount() > 1) {
        var seqs = myCodeMirror2.getValue();
        localStorage.setItem("resultcookie", seqs);
        window.location.href = "/#/tools/" + tool;
    } else { alert("No output to forward to."); }
}

function hideFormats(currentVal) {
    var ddl = $("#outformat");
    for (var m = 0; m < ddl.length; m++) {
        for (var i = 0; i < ddl[m].length; i++) {
            ddl[m].options[i].disabled = false;
        }
    }
    switch (currentVal) {
        case "Fasta":
            $("#Fasta").prop("disabled", true);
            break;
        case "Phylip":
            $("#Phylip").prop("disabled", true);
            break;
        case "Clustal":
            $("#Clustal").prop("disabled", true);
            break;
        case "Nexus":
            $("#NEXUS").prop("disabled", true);
            break;
        case "EMBL":
            $("#EMBL").prop("disabled", true);
            break;
        case "PIR":
            $("#PIR").prop("disabled", true);
            break;
        case "Stockholm":
            $("#Stockholm").prop("disabled", true);
            break;
        default:
            $(".biofmt").prop("disabled", true);
            break;
    }
}

// hides tools for forwarding that are not able to process the current format
function hideTools(currentVal) {
    var ddl = $("#forwardTool");
    for (var m = 0; m < ddl.length; m++) {
        for (var i = 0; i < ddl[m].length; i++) {
            ddl[m].options[i].disabled = false;
        }
    }
    if (currentVal === "Fasta" || currentVal === "Clustal") {
        ddl.prop("disabled", false);
        ddl.find("> :not(#retseq)").show();
        $("#retseq").hide();

    } else if (currentVal === "GetAccessions") {
        ddl.prop("disabled", false);
        ddl.find("> :not(#retseq)").hide();
        $("#retseq").show();
    } else {
        ddl.prop("disabled", true);
    }
}

/* Tool statistics */
function toolCounter() {
    m.request({
        method: "POST",
        url: "/api/frontendSubmit/Reformat"
    });
}

function forwardChanged() {
    var selectBox = document.getElementById("forwardTool");
    var selectedValue = selectBox.options[selectBox.selectedIndex].value;
    forwardTo(selectedValue);
}

$("#tool-tabs").tabs();
$("#callout").hide();

var inputClustal = "CLUSTAL multiple sequence alignment\n\ngi|33300828\tPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHI\ngi|11479639\tPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGG\ngi|11479645\tPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAY\ngi|29366706\tPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPG\ngi|68299729\tPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGH\ngi|77118174\tPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP-\ngi|17570796\tPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGE\ngi|11963775\tPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGE\n\ngi|33300828\tD---EAMPNYGRIVEVSAMERSAN-TLRHPSFSRFR\ngi|11479639\tA-DHPGMADLGRVVEVTAMERSAN-TLRHPKFSRFR\ngi|11479645\tEVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGFR\ngi|29366706\t-------YYKGHTAKVTFMERYPDGSLRHPSFDSFR\ngi|68299729\tD-----DCFNGRPVQVKYMEKTPKGSLRHPSFQRWR\ngi|77118174\t------DYYKGWACQITYMEETPDGSLRHPSFDQWR\ngi|17570796\tD------FYNGWACQVNYMEATPDGSLRHPSFEKFR\ngi|11963775\tD------YYNGWACQVAYMEETSDGSLRHPSFVMFR";

$("#format").text(getFormat(myCodeMirror.getValue()));
// on and off handler like in jQuery
myCodeMirror.on("change", function (cMirror) {
    hideFormats(getFormat(myCodeMirror.getValue()));
    // get value right from instance
    if (validateFasta(myCodeMirror.getValue())) {
        document.getElementById("callout").style.display = "block";

        if (validateAlignment(fasta2json(myCodeMirror.getValue()))) {
            $(".clustaloption").show();
            document.getElementById("nonalignedwarning").style.display = "none";
        }
        else {
            $(".clustaloption").hide();
            document.getElementById("nonalignedwarning").style.display = "inline";
        }
    }
    if (getFormat(myCodeMirror.getValue()) !== "") {
        document.getElementById("callout").style.display = "block";
        $("#format").text(getFormat(myCodeMirror.getValue()));
    }
    if (getFormat(myCodeMirror.getValue()) === "") {
        document.getElementById("callout").style.display = "none";
    }
});

document.getElementById("outformat").onchange = function () {
    var currentVal = this.value;
    hideTools(currentVal);
    myCodeMirror2.setValue(($("#inputMirror").val(myCodeMirror.getValue())).reformat(currentVal));
    tempCode = myCodeMirror.getValue();
    myCodeMirror.setValue(tempCode);
    document.getElementById("outputarea").value = myCodeMirror2.getValue();
    toolCounter(); // write usage into the database for tool stats
};

/* Export */
function downloadResult() {
    if (myCodeMirror2.getValue())
        $("a.download").attr("href", "data:application/octet-stream;content-disposition:attachment;filename=file.txt;charset=utf-8," + encodeURIComponent(myCodeMirror2.getValue()));
}

/* MSA */
function initMSA() {
    var opts = {colorscheme: {"scheme": "clustal"}};
    opts.el = document.getElementById("yourDiv");
    opts.vis = {
        conserv: false,
        overviewbox: false,
        seqlogo: true,
        labels: true,
        labelName: true,
        labelId: false,
        labelPartition: false,
        labelCheckbox: false
    };
    opts.conf = {
        dropImport: true
    };
    opts.zoomer = {
        // Alignment viewer is not scrolling with "alignmentWidth: "auto"", use fixed numbers instead or
        // use script for handling
        alignmentHeight: 525,
        alignmentWidth: 990,
        labelNameLength: 165,
        labelWidth: 85,
        labelFontsize: 10,
        labelIdLength: 75,
        menuFontsize: "12px",
        menuMarginLeft: "2px",
        menuPadding: "0px 10px 0px 0px",
        menuItemFontsize: "14px",
        menuItemLineHeight: "14px",
        autoResize: false
    };
    var noSeqs = clustalParser("CLUSTAL multiple sequence alignment\n\nID|NO\tPLEASE\nID|SEQUENCES\tENTER\nID|FOUND\tSEQVENCES");
    var inputSeqs = $("#inputMirror").val(myCodeMirror.getValue()).reformat("detect");
    opts.seqs = ( inputSeqs === "Clustal" || inputSeqs === "Fasta")
        ? (clustalParser($("#inputMirror").val(myCodeMirror.getValue()).reformat("clustal"))) : noSeqs;

    // init msa
    var ms = new msa.msa(opts);
    var menuOpts = {};
    menuOpts.el = document.getElementById("div");
    menuOpts.msa = ms;
    var defMenu = new msa.menu.defaultmenu(menuOpts);
    ms.addView("menu", defMenu);
    // call render at the end to display the whole MSA
    ms.render();
}

/* Fullscreen toggle */
var $expand = $("#expandTabReformat");
$expand.val("Expand");
var $tab = $("#tool-tabs");

function fullscreenReformat() {
    if ($expand.val() !== "Expand") {
        $tab.removeClass("fullscreen");
        $expand.val("Expand");
        if (typeof onCollapse === "function") {
            onCollapse();
        }
    } else {
        $tab.addClass("fullscreen");
        $expand.val("Collapse");
        if (typeof onExpand === "function") {
            onExpand();
        }
    }
    if (typeof onFullscreenToggle === "function") {
        return onFullscreenToggle();
    }
}

$(document).keydown(function (e) {
    if (e.keyCode === 27 && $tab.hasClass("fullscreen")) {
        $expand.click();
    }
});
