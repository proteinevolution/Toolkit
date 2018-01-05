/// <reference path="helper.ts"/>
const exampleSequence = ">AAN59974.1 histone H2A [Homo sapiens]\nMSG------------------RGKQGG-KARAKAKTRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK-----\n>NP_001005967.1 histone 2, H2a [Danio rerio]\nMSG------------------RGKTGG-KARAKAKSRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAVRNDEELNKLLGGVTIAQGGVLPNIQAVLLPKKTEKPAKSK-------\n>NP_001027366.1 histone H2A [Drosophila melanogaster]\nMSG------------------RGK-GG-KVKGKAKSRSDRAGLQFPVGRIHRLLRKGNYAERVGAGAPVYLAAVMEYLAAEVLELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLSGVTIAQGGVLPNIQAVLLPKKTEKKA----------\n>NP_175517.1 histone H2A 10 [Arabidopsis thaliana]\nMAG------------------RGKTLGSGSAKKATTRSSKAGLQFPVGRIARFLKKGKYAERVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIVPRHIQLAVRNDEELSKLLGDVTIANGGVMPNIHNLLLPKKTGASKPSAEDD----\n>NP_001263788.1 Histone H2A [Caenorhabditis elegans]\nMSG------------------RGKGGKAKTGGKAKSRSSRAGLQFPVGRLHRILRKGNYAQRVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIAPRHLQLAVRNDEELNKLLAGVTIAQGGVLPNIQAVLLPKKTGGDKEIRLSNLPKQ\n>NP_009552.1 histone H2A [Saccharomyces cerevisiae S288C]\nMSG------------------GKGGKAGSAAKASQSRSAKAGLTFPVGRVHRLLRRGNYAQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLGNVTIAQGGVLPNIHQNLLPKKSAKTAKASQEL----\n>XP_641587.1 histone H2A [Dictyostelium discoideum AX4]\nMSETKPASSKPAAAAKPKKVIPRVSRTGEPKSKPESRSARAGITFPVSRVDRLLREGRFAPRVESTAPVYLAAVLEYLVFEILELAHNTCSISKKTRITPQHINWAVGNDLELNSLFQHVTIAYGGVLPTPQQSTGEKKKKPSKKAAEGSSQIY";
// Update the value with the one from the local storage

let alignmentView: any;

interface Window {
    FrontendAlnvizComponent: any;
}

window.FrontendAlnvizComponent = {
    controller: function() {
        let submitted: boolean;
        submitted = false;
        return {
            frontendSubmit: function(): any {
                if (!submitted) {
                    return $.ajax({
                        url: '/api/frontendSubmit/Alnviz',
                        type: 'POST',
                        dataType: "text",
                        processData: false,
                        success: function() {
                            submitted = true;
                        },
                        error: function() {
                            submitted = true;
                        }
                    });
                }
            },
            initMSA: function(): any {
                let toolTabs = $('#tool-tabs');

                let height = toolTabs.hasClass('fullscreen') ? $(window).height() - 320 : toolTabs.width() - 500;
                let width = toolTabs.width() - 240;

                let seqs = $('#alignment').reformat('Fasta');
                if (!seqs) {
                    return;
                }

                let opts = {
                    colorscheme: {
                        "scheme": "clustal"
                    },
                    el: document.getElementById('bioJSContainer'),
                    vis: {
                        conserv: false,
                        overviewbox: false,
                        seqlogo: true,
                        labels: true,
                        labelName: true,
                        labelId: false,
                        labelPartition: false,
                        labelCheckbox: false
                    },
                    menu: 'small',
                    seqs: fasta2json(seqs),
                    zoomer: {
                        alignmentHeight: height,
                        alignmentWidth: width,
                        labelNameLength: 165,
                        labelWidth: 85,
                        labelFontsize: "13px",
                        labelIdLength: 75,
                        menuFontsize: "13px",
                        menuPadding: "0px 10px 0px 0px",
                        menuMarginLeft: "-6px",
                        menuItemFontsize: "14px",
                        menuItemLineHeight: "14px",
                        autoResize: true
                    }
                };

                alignmentView = new msa.msa(opts);

                let menuOpts = {
                    el: document.getElementById("menuDiv"),
                    msa: alignmentView
                };
                let defMenu = new msa.menu.defaultmenu(menuOpts);
                alignmentView.addView('menu', defMenu);
                alignmentView.render();
                $(window).on("resize.MSAViewer", function() {

                    if ($("#bioJSContainer").parents("html").length === 0) {
                        $(window).off("resize.MSAViewer");
                        return;
                    }
                    alignmentView.g.zoomer.set("alignmentWidth", toolTabs.width() - 240);
                    if (toolTabs.hasClass("fullscreen")) {
                        alignmentView.g.zoomer.set("alignmentHeight", Math.max(400, $(window).height() - 320));
                    }
                });

                setTimeout(function() {
                    $('#tab-Visualization').removeAttr('style');
                }, 100);
                return toolTabs.tabs('option', 'active', toolTabs.tabs('option', 'active') + 1);
            },
            forwardTab: function() {
                return $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') + 1);
            }
        };
    },

    view: function(ctrl: any) {
        document.title = "AlignmentViewer - Bioinformatics Toolkit";
        return m("div", {
            id: "jobview"
        }, [
            m("div", {
                    "class": "jobline"
                }, m("span", {
                    "class": "toolname"
                }, m("a", {
                    onclick: function() {
                        m.route("/tools/" + "alnviz");
                    }
                }, "AlignmentViewer")),
                m("i", {"class": "icon-white_question helpicon", "title": "Help page", "config": tooltipConf})),
            m(GeneralTabComponent, {
                tabs: ["Alignment", "Visualization"],
                ctrl: ctrl
            })
        ]);
    }
};

const fndt = function(elem: any, isInit: boolean): any {
    if (!isInit) {
        return $(elem).foundation();
    }
};

interface Window {
    FrontendReformatComponent: any;
}

window.FrontendReformatComponent = {
    controller: function() {
        return {
            html: m.request({
                method: "GET",
                url: "/static/get/reformat",
                deserialize: function(data) {
                    return data;
                }
            })
        };
    },
<<<<<<< HEAD
    view: function(ctrl: any) {
=======
    view: function(ctrl : any) {
        document.title = "Reformat - Bioinformatics Toolkit";
>>>>>>> 6aded13... Tools/Jobs have their toolname in title
        return m("div", {
            id: "jobview",
            config: fndt
        }, m.trust(ctrl.html()));
    }
};

const renderTabs = function(tabs: any, content: any) {
    return m("div", {
        "class": "tool-tabs",
        id: "tool-tabs",
        config: tabulated
    }, [
        m("ul", tabs.map(function(tab: any) {
            return m("li", {
                id: "tab-" + tab
            }, m("a", {
                href: "#tabpanel-" + tab
            }, tab));
        })), tabs.map(function(tab: any, idx: number) {
            return m("div", {
                "class": "tabs-panel",
                id: "tabpanel-" + tab
            }, content[idx]);
        })
    ]);
};

const GeneralTabComponent = {

    controller: function() {
        let mo = {
            isFullscreen: false,
            label: "Expand"
        };
        let onCollapse: any, onExpand: any, onFullscreenToggle: any;
        return {
            getLabel: (function() {
                return this.label;
            }).bind(mo),
            fullscreen: (function() {
                let job_tab_component = $("#tool-tabs");
                if (this.isFullscreen) {
                    job_tab_component.removeClass("fullscreen");
                    this.isFullscreen = false;
                    if (typeof onCollapse === "function") {
                        onCollapse();
                    }
                    $("#collapseMe").addClass("fa-expand").removeClass("fa-compress");
                    if (typeof alignmentView !== "undefined") {
                        alignmentView.g.zoomer.set("alignmentHeight", job_tab_component.width() - 500);
                        alignmentView.g.zoomer.set("alignmentWidth", job_tab_component.width() - 240);
                    }
                    followScroll(document);
                    return;
                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
                    $("#collapseMe").addClass("fa-compress").removeClass("fa-expand");
                    if (typeof alignmentView !== "undefined") {
                        alignmentView.g.zoomer.set("alignmentHeight", $(window).height() - 320);
                        alignmentView.g.zoomer.set("alignmentWidth", job_tab_component.width() - 240);
                    }
                    followScroll(job_tab_component);
                }
                if (typeof onFullscreenToggle === "function" && this.isFullscreen === true) {
                    return onFullscreenToggle();
                }
            }).bind(mo),
            forwardString: (function () {
                if (localStorage.getItem("resultcookie")) {
                    let cookieString = String(localStorage.getItem("resultcookie"));
                    localStorage.removeItem("resultcookie");
                    $.LoadingOverlay("hide");
                    return cookieString;
                } else {
                    return "";
                }
            }).bind(mo)
        };
    },
    view: function(ctrl: any, args: any) {
        return m("div", {
            "class": "tool-tabs",
            id: "tool-tabs",
            config: tabulated
        }, [
            m("ul", args.tabs.map(function(tab: any) {
                return m("li", {
                    id: "tab-" + tab,
                    style: (tab == "Visualization" ? "display: none;" : "display: block;")
                }, m("a", {
                    href: "#tabpanel-" + tab
                }, tab));
            }), m("li", {
                style: "float: right;"
            }, m("i", {
                type: "button",
                id: "collapseMe",
                "class": "fa fa-expand button_fullscreen",
                value: ctrl.getLabel(),
                onclick: ctrl.fullscreen,
                config: closeShortcut
            }))), args.tabs.map(function(tab: any) {
                return m("div", {
                    "class": "tabs-panel",
                    id: "tabpanel-" + tab
                }, tabsContents[tab](args.ctrl));
            })
        ]);
    }
};


const tabsContents: any = {
    "Alignment": function(ctrl: any) {
        return m("div", {
            "class": "parameter-panel", "id": "alignmentViewerPanel"
        }, [
            m("textarea", {
                name: "alignment",
                placeholder: "Enter sequences in FASTA or CLUSTAL format.",
                options: [["fas", "FASTA"]],
                id: "alignment",
                rows: 25,
                value: GeneralTabComponent.controller().forwardString(),
                spellcheck: false
            }),
            m("input", {
                id: "pasteButton",
                "class": "button small alignmentExample",
                value: "Paste Example",
                config: sampleSeqConfig,
                onclick: function() {
                    $('#alignment').val(exampleSequence);
                }
            }),
            m("div", {
                "class": "submitbuttons",
                onclick: ctrl.frontendSubmit
            }, m("input", {
                type: "button",
                "class": "success button small submitJob",
                value: "View Alignment",
                onclick: ctrl.initMSA,
                id: "viewAlignment"
            }))
        ]);
    },
    "Visualization": function(ctrl: any) {
        return m("div", [
            m("div", {
                id: "menuDiv"
            }), m("div", {
                id: "bioJSContainer"
            })
        ]);
    },
    "Freqs": function(ctrl: any) {
        return "Test";
    }
};
