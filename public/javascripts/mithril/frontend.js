var GeneralTabComponent, exampleSequence, fndt, renderTabs, tabsContents, tabulated;

exampleSequence = ">gi|33300828|ref|NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHI\nD---EAMPNYGRIVEVSAMERSAN-TLRHPSFSRFR\n>gi|114796395|emb|CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGG\nA-DHPGMADLGRVVEVTAMERSAN-TLRHPKFSRFR\n>gi|114796457|emb|CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAY\nEVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGFR\n>gi|29366706|ref|NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPG\n-------YYKGHTAKVTFMERYPDGSLRHPSFDSFR\n>gi|68299729|ref|YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGH\nD-----DCFNGRPVQVKYMEKTPKGSLRHPSFQRWR\n>gi|77118174|ref|YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP-\n------DYYKGWACQITYMEETPDGSLRHPSFDQWR\n>gi|17570796|ref|NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGE\nD------FYNGWACQVNYMEATPDGSLRHPSFEKFR\n>gi|119637753|ref|YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGE\nD------YYNGWACQVAYMEETSDGSLRHPSFVMFR";

window.FrontendAlnvizComponent = {
    controller: function() {
        var submitted;
        ({
            pasteExample: function() {
                return $('#alignment').val(exampleSequence);
            }
        });
        submitted = false;
        return {
            frontendSubmit: function() {
                if (!submitted) {
                    return $.ajax({
                        url: '/api/frontendSubmit/Alnviz',
                        type: 'POST',
                        success: function(result) {
                            console.log('ok');
                            submitted = true;
                        },
                        error: function(result) {
                            console.warn('error');
                            submitted = true;
                        }
                    });
                } else {

                }
            },
            initMSA: function() {
                var alignment, defMenu, menuOpts, opts, seqs;
                seqs = $('#alignment').reformat('Fasta');
                height = (seqs.split('>').length-1)*15;
                if (!seqs) {
                    return;
                }
                opts = {
                    colorscheme: {
                        "scheme": "mae"
                    },
                    el: document.getElementById('bioJSContainer'),
                    vis: {
                        conserv: false,
                        overviewbox: false,
                        seqlogo: false,
                        labels: false,
                        labelName: true,
                        labelId: false,
                        labelPartition: false,
                        labelCheckbox: false
                    },
                    menu: 'small'
                };
                opts.seqs = fasta2json(seqs);
                opts.zoomer = {
                    alignmentHeight: height,
                    alignmentWidth: 'auto',
                    labelNameLength: 165,
                    labelWidth: 85,
                    labelFontsize: "13px",
                    labelIdLength: 75,
                    menuFontsize: "12px",
                    menuMarginLeft: "2px",
                    menuPadding: "0px 10px 0px 0px",
                    menuItemFontsize: "14px",
                    menuItemLineHeight: "14px",
                    autoResize: true
                };
                alignment = new msa.msa(opts);
                menuOpts = {};
                menuOpts.el = document.getElementById('menuDiv');
                menuOpts.msa = alignment;
                defMenu = new msa.menu.defaultmenu(menuOpts);
                alignment.addView('menu', defMenu);
                alignment.render();
                return $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') + 1);
            },
            forwardTab: function() {
                return $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') + 1);
            }
        };
    },
    view: function(ctrl) {
        return m("div", {
            id: "jobview"
        }, [
            m("div", {
                "class": "jobline"
            }, m("span", {
                "class": "toolname"
            }, "Alignment Viewer")), m(GeneralTabComponent, {
                tabs: ["Alignment", "Visualization"],
                ctrl: ctrl
            })
        ]);
    }
};


fndt = function(elem, isInit) {
    if (!isInit) {
        return $(elem).foundation();
    }
};

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
    view: function(ctrl) {
        return m("div", {
            id: "jobview",
            config: fndt
        }, m.trust(ctrl.html()));
    }
};

/*
window.FrontendRetrieveSeqComponent = {
    model: function(args) {
        return {
            tabs: ["Input"],
            content: [
                m("div", {
                    "class": "parameter-panel"
                }, [m("textarea")])
            ]
        };
    },
    controller: function(args) {
        return this.model = new ParameterAlignmentComponent.model(args);
    },
    view: function(ctrl) {
        return renderTabs(ctrl.model.tabs, ctrl.model.content);
    }
}; */


/*

 $(document).foundation()
 $("html, body").animate({ scrollTop: 0 }, "fast")

 window.FrontendReformatComponent =
 controller: ->

 view: (ctrl) ->
 m "div", {id: "jobview"}, [

 m "div", {class: "jobline"},
 m "span", {class: "toolname"}, "Reformat"

 m GeneralTabComponent, {tabs: ["Alignment", "Freqs"], ctrl: ctrl}
 ]
 */

renderTabs = function(tabs, content) {
    return m("div", {
        "class": "tool-tabs",
        id: "tool-tabs",
        config: tabulated
    }, [
        m("ul", tabs.map(function(tab) {
            return m("li", {
                id: "tab-" + tab
            }, m("a", {
                href: "#tabpanel-" + tab
            }, tab));
        })), tabs.map(function(tab, idx) {
            return m("div", {
                "class": "tabs-panel",
                id: "tabpanel-" + tab
            }, content[idx]);
        })
    ]);
};

GeneralTabComponent = {
    model: function() {
        return {
            isFullscreen: false,
            label: "Expand"
        };
    },
    controller: function() {
        var mo = new GeneralTabComponent.model();
        return {
            getLabel: (function() {
                return this.label;
            }).bind(mo),
            fullscreen: (function() {
                var job_tab_component;
                job_tab_component = $("#tool-tabs");
                if (this.isFullscreen) {
                    job_tab_component.removeClass("fullscreen");
                    this.isFullscreen = false;
                    this.label = "Expand";
                    if (typeof onCollapse === "function") {
                        onCollapse();
                    }
                    $("#collapseMe").addClass("fa-expand").removeClass("fa-compress");
                    $('#bioJSContainer').css({'overflow-x': 'scroll'});
                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    this.label = "Collapse";
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
                    $("#collapseMe").addClass("fa-compress").removeClass("fa-expand");
                    $('#bioJSContainer').css({'overflow-x': 'auto'});
                }
                if (typeof onFullscreenToggle === "function") {
                    return onFullscreenToggle();
                }
            }).bind(mo)
        };
    },
    view: function(ctrl, args) {
        return m("div", {
            "class": "tool-tabs",
            id: "tool-tabs",
            config: tabulated
        }, [
            m("ul", args.tabs.map(function(tab) {
                return m("li", {
                    id: "tab-" + tab
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
            }))), args.tabs.map(function(tab) {
                return m("div", {
                    "class": "tabs-panel",
                    id: "tabpanel-" + tab
                }, tabsContents[tab](args.ctrl));
            })
        ]);
    }
};


/*
 m "div", {class: "tabs-panel", id: "tabpanel-#{paramGroup[0]}"}, [
 */

tabsContents = {
    "Alignment": function(ctrl) {
        return m("div", {
            "class": "parameter-panel", "id": "alignmentViewerPanel"
        }, [
            m("textarea", {
                name: "alignment",
                placeholder: "multiple sequence alignment",
                options: [["fas", "FASTA"]],
                id: "alignment",
                rows: 25,
                value: "",
                spellcheck: false
            }), m("div", {
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
    "Visualization": function(ctrl) {
        return m("div", [
            m("div", {
                id: "menuDiv"
            }), m("div", {
                id: "bioJSContainer"
            })
        ]);
    },
    "Freqs": function(ctrl) {
        return "Test";
    }
};

