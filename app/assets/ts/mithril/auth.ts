let jqueryUITabsInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).tabs();
    }
};
let foundationDropdownInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).foundation();
    }
};

let regions = [ ["","Country"],
    ["AFG","Afghanistan"],
    ["ALA","\305land Islands"],
    ["ALB","Albania"],
    ["DZA","Algeria"],
    ["ASM","American Samoa"],
    ["AND","Andorra"],
    ["AGO","Angola"],
    ["AIA","Anguilla"],
    ["ATA","Antarctica"],
    ["ATG","Antigua and Barbuda"],
    ["ARG","Argentina"],
    ["ARM","Armenia"],
    ["ABW","Aruba"],
    ["AUS","Australia"],
    ["AUT","Austria"],
    ["AZE","Azerbaijan"],
    ["BHS","Bahamas"],
    ["BHR","Bahrain"],
    ["BGD","Bangladesh"],
    ["BRB","Barbados"],
    ["BLR","Belarus"],
    ["BEL","Belgium"],
    ["BLZ","Belize"],
    ["BEN","Benin"],
    ["BMU","Bermuda"],
    ["BTN","Bhutan"],
    ["BOL","Bolivia, Plurinational State of"],
    ["BES","Bonaire, Sint Eustatius and Saba"],
    ["BIH","Bosnia and Herzegovina"],
    ["BWA","Botswana"],
    ["BVT","Bouvet Island"],
    ["BRA","Brazil"],
    ["IOT","British Indian Ocean Territory"],
    ["BRN","Brunei Darussalam"],
    ["BGR","Bulgaria"],
    ["BFA","Burkina Faso"],
    ["BDI","Burundi"],
    ["KHM","Cambodia"],
    ["CMR","Cameroon"],
    ["CAN","Canada"],
    ["CPV","Cape Verde"],
    ["CYM","Cayman Islands"],
    ["CAF","Central African Republic"],
    ["TCD","Chad"],
    ["CHL","Chile"],
    ["CHN","China"],
    ["CXR","Christmas Island"],
    ["CCK","Cocos (Keeling) Islands"],
    ["COL","Colombia"],
    ["COM","Comoros"],
    ["COG","Congo"],
    ["COD","Congo, the Democratic Republic of the"],
    ["COK","Cook Islands"],
    ["CRI","Costa Rica"],
    ["CIV","C\364te d'Ivoire"],
    ["HRV","Croatia"],
    ["CUB","Cuba"],
    ["CUW","Cura\347ao"],
    ["CYP","Cyprus"],
    ["CZE","Czech Republic"],
    ["DNK","Denmark"],
    ["DJI","Djibouti"],
    ["DMA","Dominica"],
    ["DOM","Dominican Republic"],
    ["ECU","Ecuador"],
    ["EGY","Egypt"],
    ["SLV","El Salvador"],
    ["GNQ","Equatorial Guinea"],
    ["ERI","Eritrea"],
    ["EST","Estonia"],
    ["ETH","Ethiopia"],
    ["FLK","Falkland Islands (Malvinas)"],
    ["FRO","Faroe Islands"],
    ["FJI","Fiji"],
    ["FIN","Finland"],
    ["FRA","France"],
    ["GUF","French Guiana"],
    ["PYF","French Polynesia"],
    ["ATF","French Southern Territories"],
    ["GAB","Gabon"],
    ["GMB","Gambia"],
    ["GEO","Georgia"],
    ["DEU","Germany"],
    ["GHA","Ghana"],
    ["GIB","Gibraltar"],
    ["GRC","Greece"],
    ["GRL","Greenland"],
    ["GRD","Grenada"],
    ["GLP","Guadeloupe"],
    ["GUM","Guam"],
    ["GTM","Guatemala"],
    ["GGY","Guernsey"],
    ["GIN","Guinea"],
    ["GNB","Guinea-Bissau"],
    ["GUY","Guyana"],
    ["HTI","Haiti"],
    ["HMD","Heard Island and McDonald Islands"],
    ["VAT","Holy See (Vatican City State)"],
    ["HND","Honduras"],
    ["HKG","Hong Kong"],
    ["HUN","Hungary"],
    ["ISL","Iceland"],
    ["IND","India"],
    ["IDN","Indonesia"],
    ["IRN","Iran, Islamic Republic of"],
    ["IRQ","Iraq"],
    ["IRL","Ireland"],
    ["IMN","Isle of Man"],
    ["ISR","Israel"],
    ["ITA","Italy"],
    ["JAM","Jamaica"],
    ["JPN","Japan"],
    ["JEY","Jersey"],
    ["JOR","Jordan"],
    ["KAZ","Kazakhstan"],
    ["KEN","Kenya"],
    ["KIR","Kiribati"],
    ["PRK","Korea, Democratic People's Republic of"],
    ["KOR","Korea, Republic of"],
    ["KWT","Kuwait"],
    ["KGZ","Kyrgyzstan"],
    ["LAO","Lao People's Democratic Republic"],
    ["LVA","Latvia"],
    ["LBN","Lebanon"],
    ["LSO","Lesotho"],
    ["LBR","Liberia"],
    ["LBY","Libya"],
    ["LIE","Liechtenstein"],
    ["LTU","Lithuania"],
    ["LUX","Luxembourg"],
    ["MAC","Macao"],
    ["MKD","Macedonia, the former Yugoslav Republic of"],
    ["MDG","Madagascar"],
    ["MWI","Malawi"],
    ["MYS","Malaysia"],
    ["MDV","Maldives"],
    ["MLI","Mali"],
    ["MLT","Malta"],
    ["MHL","Marshall Islands"],
    ["MTQ","Martinique"],
    ["MRT","Mauritania"],
    ["MUS","Mauritius"],
    ["MYT","Mayotte"],
    ["MEX","Mexico"],
    ["FSM","Micronesia, Federated States of"],
    ["MDA","Moldova, Republic of"],
    ["MCO","Monaco"],
    ["MNG","Mongolia"],
    ["MNE","Montenegro"],
    ["MSR","Montserrat"],
    ["MAR","Morocco"],
    ["MOZ","Mozambique"],
    ["MMR","Myanmar"],
    ["NAM","Namibia"],
    ["NRU","Nauru"],
    ["NPL","Nepal"],
    ["NLD","Netherlands"],
    ["NCL","New Caledonia"],
    ["NZL","New Zealand"],
    ["NIC","Nicaragua"],
    ["NER","Niger"],
    ["NGA","Nigeria"],
    ["NIU","Niue"],
    ["NFK","Norfolk Island"],
    ["MNP","Northern Mariana Islands"],
    ["NOR","Norway"],
    ["OMN","Oman"],
    ["PAK","Pakistan"],
    ["PLW","Palau"],
    ["PSE","Palestinian Territory, Occupied"],
    ["PAN","Panama"],
    ["PNG","Papua New Guinea"],
    ["PRY","Paraguay"],
    ["PER","Peru"],
    ["PHL","Philippines"],
    ["PCN","Pitcairn"],
    ["POL","Poland"],
    ["PRT","Portugal"],
    ["PRI","Puerto Rico"],
    ["QAT","Qatar"],
    ["REU","R\351union"],
    ["ROU","Romania"],
    ["RUS","Russian Federation"],
    ["RWA","Rwanda"],
    ["BLM","Saint Barth\351lemy"],
    ["SHN","Saint Helena, Ascension and Tristan da Cunha"],
    ["KNA","Saint Kitts and Nevis"],
    ["LCA","Saint Lucia"],
    ["MAF","Saint Martin (French part)"],
    ["SPM","Saint Pierre and Miquelon"],
    ["VCT","Saint Vincent and the Grenadines"],
    ["WSM","Samoa"],
    ["SMR","San Marino"],
    ["STP","Sao Tome and Principe"],
    ["SAU","Saudi Arabia"],
    ["SEN","Senegal"],
    ["SRB","Serbia"],
    ["SYC","Seychelles"],
    ["SLE","Sierra Leone"],
    ["SGP","Singapore"],
    ["SXM","Sint Maarten (Dutch part)"],
    ["SVK","Slovakia"],
    ["SVN","Slovenia"],
    ["SLB","Solomon Islands"],
    ["SOM","Somalia"],
    ["ZAF","South Africa"],
    ["SGS","South Georgia and the South Sandwich Islands"],
    ["SSD","South Sudan"],
    ["ESP","Spain"],
    ["LKA","Sri Lanka"],
    ["SDN","Sudan"],
    ["SUR","Suriname"],
    ["SJM","Svalbard and Jan Mayen"],
    ["SWZ","Swaziland"],
    ["SWE","Sweden"],
    ["CHE","Switzerland"],
    ["SYR","Syrian Arab Republic"],
    ["TWN","Taiwan, Province of China"],
    ["TJK","Tajikistan"],
    ["TZA","Tanzania, United Republic of"],
    ["THA","Thailand"],
    ["TLS","Timor-Leste"],
    ["TGO","Togo"],
    ["TKL","Tokelau"],
    ["TON","Tonga"],
    ["TTO","Trinidad and Tobago"],
    ["TUN","Tunisia"],
    ["TUR","Turkey"],
    ["TKM","Turkmenistan"],
    ["TCA","Turks and Caicos Islands"],
    ["TUV","Tuvalu"],
    ["UGA","Uganda"],
    ["UKR","Ukraine"],
    ["ARE","United Arab Emirates"],
    ["GBR","United Kingdom"],
    ["USA","United States"],
    ["UMI","United States Minor Outlying Islands"],
    ["URY","Uruguay"],
    ["UZB","Uzbekistan"],
    ["VUT","Vanuatu"],
    ["VEN","Venezuela, Bolivarian Republic of"],
    ["VNM","Viet Nam"],
    ["VGB","Virgin Islands, British"],
    ["VIR","Virgin Islands, U.S."],
    ["WLF","Wallis and Futuna"],
    ["ESH","Western Sahara"],
    ["YEM","Yemen"],
    ["ZMB","Zambia"],
    ["ZWE","Zimbabwe"] ];

class SignIn {
    static nameLogin : string = "";
    static password  : string = "";
    static nameLoginSetter (nameLogin : string) : void { SignIn.nameLogin = nameLogin; }
    static passwordSetter  (password  : string) : void { SignIn.password  = password;  }

    static submit(event : Event) : void {
        event.preventDefault();
        var dataS = {nameLogin:SignIn.nameLogin, password:SignIn.password};
        m.request({method: "POST", url: "signin", data: dataS }).then(function(authMessage) {
            //type : AuthMessage
            if (authMessage.successful) {
                SignIn.password = null;
                LiveTable.updateJobInfo();
                JobListComponent.reloadList();
            }
            Auth.updateStatus(authMessage);
        });
    }

    static controller (args : any) : any {
        return {}
    }
    static view (ctrl : any, args : any) : any {
        return m("div", { class : "auth-form" },
            m("form", { 'data-abide': 'ajax', id: 'signin-form', novalidate:'novalidate', onsubmit: SignIn.submit }, [
                m("div", m("label",
                    m("input", { id:         'nameLogin',
                                 name:       'nameLogin',
                                 pattern:    '[a-zA-Z0-9_]{6,40}',
                                 placeholder:'Username',
                                 required:   'required',
                                 type:       'text',
                                 onkeyup:    m.withAttr("value", SignIn.nameLoginSetter),
                                 onchange:   m.withAttr("value", SignIn.nameLoginSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:      SignIn.nameLogin
                    })
                )),
                m("div", m("label",
                    m("input", { id:         'password',
                                 name:       'password',
                                 pattern:    '.{8,40}',
                                 placeholder:'Password',
                                 required:   'required',
                                 type:       'password',
                                 onkeyup:    m.withAttr("value", SignIn.passwordSetter),
                                 onchange:   m.withAttr("value", SignIn.passwordSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:      SignIn.password
                    })
                )),
                m("input", { class: "input small expanded secondary button",
                             id:    'signin-submit',
                             type:  'submit',
                             value: 'Sign In'
                })
            ])
        )
    }
}

class SignUp {
    static nameLogin : string = "";
    static eMail     : string = "";
    static password  : string = "";
    static acceptToS : boolean = false;
    static formValid : boolean = false;
    static nameLoginSetter(nameLogin  : string)  : void { SignUp.nameLogin = nameLogin; }
    static eMailSetter    (eMail      : string)  : void { SignUp.eMail     = eMail;     }
    static passwordSetter (password   : string)  : void { SignUp.password  = password;  }
    static acceptToSSetter(acceptToS? : boolean) : void { SignUp.acceptToS = acceptToS ? acceptToS : !SignUp.acceptToS; }

    static controller (args : any) : any {
        if (args) {
            SignUp.nameLogin = args.nameLogin ? args.nameLogin : SignUp.nameLogin;
            SignUp.eMail     = args.eMail     ? args.eMail     : SignUp.eMail;
            SignUp.password  = args.password  ? args.password  : SignUp.password;
        }
        return { nameLogin : SignUp.nameLogin, eMail : SignUp.eMail, password : SignUp.password }
    }

    static validate(event : Event) : boolean {
        if (!SignUp.acceptToS) {
            console.log("invalid ToS");
            return SignUp.formValid = false;
        }
        $("#signup-form").find(':input').each(function() : any {
            if (!this.value && this.type !== "submit") { console.log("null Input"); return SignUp.formValid = false; }
        });
        $(".is-invalid-input").each(function() { console.log("invalid Input"); return SignUp.formValid = false; });
        return SignUp.formValid = true;
    }

    static submit(event : Event) : void {
        event.preventDefault();
        if (SignUp.formValid || SignUp.validate(event)) {
            var dataS = {nameLogin : SignUp.nameLogin,
                         password  : SignUp.password,
                         eMail     : SignUp.eMail,
                         acceptToS : SignUp.acceptToS };
            m.request({method: "POST", url: "signup", data: dataS }).then(function(authMessage) {
                //type : AuthMessage
                Auth.updateStatus(authMessage);
            });
        }
    }

    static view (ctrl : any, args : any) : any {
        return m("div", {class: "auth-form"},
            m("form", {'data-abide': 'ajax',
                        class:       'auth-form',
                        id:          'signup-form',
                        onsubmit:    SignUp.submit,
                        onchange:    SignUp.validate,
                        novalidate:  'novalidate'
            }, [
                m("div", m("label", [
                    m("input", { id:         'nameLogin',
                                 name:       'nameLogin',
                                 pattern:    '[a-zA-Z0-9_]{6,40}',
                                 placeholder:'Username',
                                 required:   'required',
                                 type:       'text',
                                 onkeyup:     m.withAttr("value", SignUp.nameLoginSetter),
                                 onchange:    m.withAttr("value", SignUp.nameLoginSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:       SignUp.nameLogin
                    }),
                    m("span", {class:"form-error"}, "Username must be at least 6 characters long!")
                ])),
                m("div", m("label", [
                    m("input", { id:         'passwordCheck',
                                 pattern:    '.{8,128}',
                                 placeholder:'Password',
                                 required:   'required',
                                 type:       'password',
                                 onkeyup:     m.withAttr("value", SignUp.passwordSetter),
                                 onchange:    m.withAttr("value", SignUp.passwordSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:       SignUp.password
                    }),
                    m("span", {class:"form-error"}, "Passwords must be at least 8 characters long!")
                ])),
                m("div", m("label", [
                    m("input", { id:             'password',
                                 name:           'password',
                                 pattern:        '.{8,128}',
                                 placeholder:    'Confirm password',
                                 'data-equalto': 'passwordCheck',
                                 required:       'required',
                                 type:           'password',
                                 onfocus:        focusInNoRedraw,
                                 onblur:         focusOutRedraw
                    }),
                    m("span", {class:"form-error"}, "Passwords must match!")
                ])),
                m("div", m("label", [
                    m("input", { id:          'eMail',
                                 name:        'eMail',
                                 pattern:     'email',
                                 placeholder: 'E-Mail',
                                 required:    'required',
                                 type:        'text',
                                 onkeyup:     m.withAttr("value", SignUp.eMailSetter),
                                 onchange:    m.withAttr("value", SignUp.eMailSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:       SignUp.eMail
                    }),
                    m("span", {class:"form-error"}, "Please enter a valid e-Mail address!")
                ])),
                m("div", m("label", {id:'checklabel'}, [
                    m("input", { id:       'acceptToS',
                                 name:     'acceptToS',
                                 required: 'required',
                                 type:     'checkbox',
                                 onchange: m.withAttr("checked", SignUp.acceptToSSetter),
                                 value:    SignUp.acceptToS
                    }),
                    "I Accept the Terms of Sevice",
                    m("span", {class:"form-error", id:'acceptToSText'}, "You must accept the ToS!")
                ])),
                m("input", { class: "input small expanded secondary button" + (SignUp.formValid? "" : " disabled"),
                             id:    'signup-submit',
                             type:  'submit',
                             value: 'Register'
                })
            ])
        )
    }
}

class ForgotPassword {
    static nameLogin : string = "";
    static eMail     : string = "";
    static nameLoginSetter (nameLogin  : string) : void { ForgotPassword.nameLogin = nameLogin; }
    static eMailSetter     (eMail      : string) : void { ForgotPassword.eMail     = eMail;     }

    static submit(event : Event) : void {
        event.preventDefault();
        var dataS = {nameLogin:ForgotPassword.nameLogin, eMail:ForgotPassword.eMail};
        console.log(dataS);
        m.request({method: "POST", url: "forgotPassword", data: dataS }).then(function(authMessage) {
            if (authMessage.successful) { SignIn.password = null }
            Auth.updateStatus(authMessage);
        });
    }

    static controller (args : any) : any {
        return {}
    }
    static view (ctrl : any, args : any) : any {
        return m("div",
            m("form", { 'data-abide': 'ajax',
                        id:           'forgot-form',
                        class:        "auth-form",
                        novalidate:   'novalidate',
                        onsubmit:     ForgotPassword.submit
            }, [
                m("div", m("label",
                    m("input", { id:          'nameLogin',
                                 name:        'nameLogin',
                                 pattern:     '[a-zA-Z0-9_]{6,40}',
                                 placeholder: 'Username',
                                 type:        'text',
                                 onkeyup:     m.withAttr("value", ForgotPassword.nameLoginSetter),
                                 onchange:    m.withAttr("value", ForgotPassword.nameLoginSetter),
                                 onfocus:     focusInNoRedraw,
                                 onblur:      focusOutRedraw,
                                 value:       ForgotPassword.nameLogin
                    })
                )),
                m("div", m("label",
                    m("input", { id:          'eMail',
                                 name:        'eMail',
                                 pattern:     'email',
                                 placeholder: 'E-Mail',
                                 type:        'text',
                                 onkeyup:      m.withAttr("value", ForgotPassword.eMailSetter),
                                 onchange:     m.withAttr("value", ForgotPassword.eMailSetter),
                                 onfocus:      focusInNoRedraw,
                                 onblur:       focusOutRedraw,
                                 value:        ForgotPassword.eMail
                    })
                )),
                m("input", { class: "input small expanded secondary button",
                             id:    'signin-submit',
                             type:  'submit',
                             value: 'Sign In'
                })
            ])
        )
    }
}

class Profile {
    static user : User   = null;
    static password  : string = "";
    static userSetter(property : string) : any { return function (value : string) : any {
        Profile.user[property] = value;
        console.log("User " + property + " is now ", Profile.user[property])
    }}
    static passwordSetter (password   : string)  : void { Profile.password  = password; }

    static submit(event : Event) : void {
        event.preventDefault();
        var userwithpw : any = { password:Profile.password };
        for (var prop in Profile.user){ if (!prop.split("_")[1]){ userwithpw[prop] = Profile.user[prop]; } }
        console.log(userwithpw);
        m.request({method: "POST", url: "profile", data: userwithpw }).then(function(authMessage) {
            Auth.updateStatus(authMessage);
        });
    }

    static controller (args : any) : any {
        Profile.user = Profile.user ? Profile.user : jQuery.extend({},Auth.user); // Needed to make a proper copy
        if (args) {
            SignUp.eMail     = args.eMail     ? args.eMail     : SignUp.eMail;
        }
        return { eMail : SignUp.eMail, user : Profile.user }
    }

    static view (ctrl : any, args : any) : any {
        if (Auth.user == null) {
            return m("div")
        } else {
            return m("div", { class: "auth-form"},
                m("form", { 'data-abide': 'ajax',
                            id:           'profile-edit-form',
                            class:        'auth-form',
                            novalidate:   'novalidate',
                            onsubmit:     Profile.submit
                }, [
                    // TODO might implement this to change the user Name in a easy and fast way
                    //m("div", {class: "small-6 small-offset-3 columns" }, m("label", [
                    //    m("input", { id:          'nameLogin',
                    //                 name:        'nameLogin',
                    //                 pattern:     '[a-zA-Z0-9_]{6,40}',
                    //                 placeholder: 'Username',
                    //                 required:    'required',
                    //                 type:        'text',
                    //                 onkeyup:     m.withAttr("value", Profile.userSetter("nameLogin")),
                    //                 onchange:    m.withAttr("value", Profile.userSetter("nameLogin")),
                    //                 onfocus:     focusInNoRedraw,
                    //                 onblur:      focusOutRedraw,
                    //                 value:       Profile.user.nameLogin
                    //    }),
                    //    m("span", {class:"form-error"}, "Username must be at least 6 characters long!")
                    //])),
                    m("div", m("label", [
                        m("input", { id:          'nameFirst',
                                     name:        'nameFirst',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'First Name',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("nameFirst")),
                                     onchange:    m.withAttr("value", Profile.userSetter("nameFirst")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.nameFirst
                        }),
                        m("span", { class:"form-error"}, "First name can be only 100 characters long!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'nameLast',
                                     name:        'nameLast',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'Last Name',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("nameLast")),
                                     onchange:    m.withAttr("value", Profile.userSetter("nameLast")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.nameLast
                        }),
                        m("span", { class:"form-error"}, "Last name can be only 100 characters long!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'eMail',
                                     name:        'eMail',
                                     pattern:     'email',
                                     placeholder: 'E-Mail',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("eMail")),
                                     onchange:    m.withAttr("value", Profile.userSetter("eMail")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.eMail
                        }),
                        m("span", { class:"form-error"}, "Please enter a valid e-Mail address!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'institute',
                                     name:        'institute',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'Institute',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("institute")),
                                     onchange:    m.withAttr("value", Profile.userSetter("institute")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.institute
                        }),
                        m("span", { class:"form-error"}, "The name of the institute can be no longer then 100 characters!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'street',
                                     name:        'street',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'Street',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("street")),
                                     onchange:    m.withAttr("value", Profile.userSetter("street")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.street
                        }),
                        m("span", { class:"form-error"}, "Please ensure that the name of the street is not longer then 100 characters!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'city',
                                     name:        'city',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'City',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("city")),
                                     onchange:    m.withAttr("value", Profile.userSetter("city")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.city
                        }),
                        m("span", { class:"form-error"}, "Please use less then 100 characters for the name of the City!")
                    ])),
                    m("div", { class: "country_drop" },
                        m("select", { name:"country", onchange: m.withAttr("value", Profile.userSetter("country")) },
                            regions.map(function(country){
                                return m("option", {
                                    value:    country[0],
                                    selected:(country[0] === Profile.user.country ? "selected" : null)
                                }, country[1])
                            })
                        )
                    ),
                    m("div", m("label", [
                        m("input", { id:          'groups',
                                     name:        'groups',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'Groups',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("groups")),
                                     onchange:    m.withAttr("value", Profile.userSetter("groups")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.groups
                        }),
                        m("span", { class:"form-error"}, "Please describe your group in less then 100 characters!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:          'roles',
                                     name:        'roles',
                                     pattern:     '[a-zA-Z0-9_]{0,100}',
                                     placeholder: 'Roles',
                                     type:        'text',
                                     onkeyup:     m.withAttr("value", Profile.userSetter("roles")),
                                     onchange:    m.withAttr("value", Profile.userSetter("roles")),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.user.roles
                        }),
                        m("span", { class:"form-error"}, "Please describe your role in less then 100 characters!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:         'password',
                                     pattern:    '.{8,128}',
                                     placeholder:'Password',
                                     required:   'required',
                                     type:       'password',
                                     onkeyup:     m.withAttr("value", Profile.passwordSetter),
                                     onchange:    m.withAttr("value", Profile.passwordSetter),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       Profile.password
                        })
                    ])),
                    m("input", { class: "input small expanded secondary button",
                                 id:    'edit-form-submit',
                                 type:  'submit',
                                 value: 'Edit Profile'
                    })
                ])
            )
        }
    }
}

class PasswordChange {
    static passwordOld      : string = "";
    static passwordNew      : string = "";
    static passwordConfirm  : string = "";
    static passwordOldSetter (password : string)  : void { PasswordChange.passwordOld  = password; }
    static passwordNewSetter (password : string)  : void { PasswordChange.passwordNew  = password; }
    static passwordConfirmSetter (password : string)  : void { PasswordChange.passwordConfirm  = password; }

    static submit(event : Event) : void {
        event.preventDefault();
        var password : any = { passwordOld : PasswordChange.passwordOld, passwordNew : PasswordChange.passwordNew };
        m.request({method: "POST", url: "password", data: password }).then(function(authMessage) {
            if (authMessage.success) {
                PasswordChange.passwordOld     = "";
                PasswordChange.passwordNew     = "";
                PasswordChange.passwordConfirm = "";
            }
            Auth.updateStatus(authMessage);
        });
    }

    static controller (args : any) : any {
        return { }
    }

    static view (ctrl : any, args : any) : any {
        if (Auth.user == null) {
            return m("div")
        } else {
            return m("div", { class: "auth-form"},
                m("form", { 'data-abide': 'ajax',
                            id:           'password-edit-form',
                            class:        'password-edit-form',
                            onsubmit:     PasswordChange.submit
                }, [
                    m("div", m("label", [
                        m("input", { id:         'passwordOld',
                                     pattern:    '.{8,128}',
                                     placeholder:'Old Password',
                                     required:   'required',
                                     type:       'password',
                                     onkeyup:     m.withAttr("value", PasswordChange.passwordOldSetter),
                                     onchange:    m.withAttr("value", PasswordChange.passwordOldSetter),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       PasswordChange.passwordOld
                        }),
                        m("span", { class:"form-error"}, "Passwords must be at least 8 characters long!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:         'passwordCheck',
                                     pattern:    '.{8,128}',
                                     placeholder:'New Password',
                                     required:   'required',
                                     type:       'password',
                                     onkeyup:     m.withAttr("value", PasswordChange.passwordNewSetter),
                                     onchange:    m.withAttr("value", PasswordChange.passwordNewSetter),
                                     onfocus:     focusInNoRedraw,
                                     onblur:      focusOutRedraw,
                                     value:       PasswordChange.passwordNew
                        }),
                        m("span", { class:"form-error"}, "Passwords must be at least 8 characters long!")
                    ])),
                    m("div", m("label", [
                        m("input", { id:             'password',
                                     name:           'password',
                                     pattern:        '.{8,128}',
                                     placeholder:    'Confirm password',
                                     'data-equalto': 'passwordCheck',
                                     required:       'required',
                                     type:           'password',
                                     onkeyup:        m.withAttr("value", PasswordChange.passwordConfirmSetter),
                                     onchange:       m.withAttr("value", PasswordChange.passwordConfirmSetter),
                                     onfocus:        focusInNoRedraw,
                                     onblur:         focusOutRedraw,
                                     value:          PasswordChange.passwordConfirm
                        }),
                        m("span", { class:"form-error"}, "Passwords must match!")
                    ])),
                    m("input", { class: "input small expanded secondary button",
                        id:    'password-edit-submit',
                        type:  'submit',
                        value: 'Change Password'
                    })
                ])
            )
        }
    }
}

// Auth dropdown
class AuthDropdown {
    static controller (args : any) : any {
        return {}
    }
    static view (ctrl : any, args : any) : any {
        if (Auth.user == null) {
            return m("button", { id:"auth-link", onclick: openNav}, "Sign in")
        } else {
            return m("div", { id:    "auth-dropdown", config: foundationDropdownInit },
                    m("ul", { id:    "auth-dropdown-link",
                              class: "dropdown menu",
                              'data-dropdown-menu': 'data-dropdown-menu',
                              'data-alignment':     "right",
                              'data-disable-hover': true,
                              'data-click-open':    true
                    }, m("li", [
                        m("button", { class : "loggedIn", id: "auth-link-text"}, Auth.user.nameLogin),
                        m("ul", { class :"menu" }, [
                            m("li", m("a", { href:"/#/" }, "Profile")),
                            m("li", m("a", { href:"/#/" }, "Inbox")),
                            m("li", m("a", { onclick:openNav }, "User")),
                            //Auth.user.isSuperUser() ? m("li", m("a", { href:"/#/backend/index" }, "Backend")) : null,
                            m("li", m("a", { href:"/signout" }, "Log Out"))
                        ])
                    ])
                )
            )
        }
    }
}

// Auth Tab components
class LoginTabs {
    static controller (args : any) : any {
        return {}
    }

    static view (ctrl : any, args : any) : any {
        return m("div", {class:"auth-tabs", id:"login-tabs", config: jqueryUITabsInit}, [
            m("ul", {class:"auth-tab"}, [
                m("li", m("a", {href:"#auth-tab-signin"}, "Sign In")),
                m("li", m("a", {href:"#auth-tab-signup"}, "Sign Up")),
                m("li", m("a", {href:"#auth-tab-forgot"}, "Reset Password"))
            ]),
            Auth.message == null ? null :
                !Auth.message.successful ?
                    m("div", {class:"callout alert", id:"auth-alert", onclick:Auth.resetStatus()}, Auth.message.message) :
                    m("div", {class:"callout",       id:"auth-alert", onclick:Auth.resetStatus()}, Auth.message.message),
            m("div", {class:"tabs-panel", id:"auth-tab-signin"}, m.component(SignIn, {})),
            m("div", {class:"tabs-panel", id:"auth-tab-signup"}, m.component(SignUp, {})),
            m("div", {class:"tabs-panel", id:"auth-tab-forgot"}, m.component(ForgotPassword, {}))
        ])
    }
}

class ProfileTabs {
    static controller (args : any) : any {
        if (args) {
        }
        return { }
    }

    static view (ctrl : any, args : any) : any {
        return m("div", {class:"auth-tabs", id: "profile-tabs", config: jqueryUITabsInit}, [
            m("ul", {id:"auth-tab"}, [
                m("li", m("a", {href:"#auth-tab-user"}, Auth.user.nameLogin)),
                m("li", m("a", {href:"#auth-tab-edit"}, "Edit Profile")),
                m("li", m("a", {href:"#auth-tab-password"}, "Change Password"))
            ]),
            Auth.message == null ? null :
                m("div", {class:   "callout" + Auth.message.successful ? "" : " alert",
                          id  :    "auth-alert",
                          onclick: Auth.resetStatus()}, Auth.message.message),
            m("div", {class:"tabs-panel", id:"auth-tab-user"}, m("div", [
                m("p", {id:"eMailDisplay"}, Auth.user.eMail[0]),
                m("input", { type:  "submit",
                             class: "small expanded secondary button",
                             onClick: function(e : Event) { window.location.href="/signout" },
                             value: "Sign Out" })
            ])),
            m("div", {class:"tabs-panel", id:"auth-tab-edit"},     m.component(Profile, {})),
            m("div", {class:"tabs-panel", id:"auth-tab-password"}, m.component(PasswordChange, {}))
        ])
    }
}

class AuthOverlay {
    static controller (args : any) : any {
        if (args) {
        }
        return { }
    }

    static view (ctrl : any, args : any) : any {
        return m("div", {id: "auth-overlay"},
            Auth.user == null ? m.component(LoginTabs, {}) : m.component(ProfileTabs, {})
        )
    }
}

class Auth {
    static user    : User = null;
    static message : AuthMessage = null;
    static changeUser (user : User) : User {
        Auth.user = user;
        var timer = setTimeout(Auth.resetStatus(), 2000);
        return Auth.user
    }
    static loadUser () : any {
        console.log("Requesting userdata");
        return m.request({method: "GET", url: "userData", type : User }).then(function(user) {
            //type : AuthMessage
            console.log("user: ", user);
            if (user) {
                SignIn.password = null;
                Auth.user       = user.nameLogin != null ? user : null;
            }
            m.mount(document.getElementById('metauser'), AuthDropdown);
            m.mount(document.getElementById('mithril-overlay-content'), AuthOverlay);
        }).catch(function(error) {
            console.log(error);
        });
    }
    static updateStatus(authMessage : AuthMessage) : void {
        console.log("updating status!", authMessage);
        Auth.message = authMessage;
        Auth.loadUser();
    }
    static resetStatus() : Function {
        return function(e : Event) { Auth.message = null }
    }
}

class AuthMessage {
    constructor (public message : string, public successful : boolean, public user : User = null) {
        console.log("new message: ", message, successful, user)
    }
}

class LoginObject {
    constructor (private _nameLogin : string = "", private _password : string = "") {
        console.log("new login object")
    }
    get nameLogin (): string { return this._nameLogin }
    get password  (): string { return this._password  }
    public nameLoginSetter (nameLogin : string) : string { this._nameLogin = nameLogin ; console.log(this._nameLogin); return this._nameLogin}
    public passwordSetter  (password  : string) : string { this._password  = password  ; console.log(this._password);  return this._password}
}

class User {
    [key: string]: string | Function | Array<string> ;
    private _nameLogin  : string = null;
    private _nameFirst  : string = null;
    private _nameLast   : string = null;
    private _eMail      : Array<string> = null;
    private _institute  : string = null;
    private _street     : string = null;
    private _city       : string = null;
    private _country    : string = null;
    private _groups     : string = null;
    private _roles      : string = null;
    constructor(object? : any){
        if (object) {
            this._nameLogin = object.nameLogin;
            this._nameFirst = object.nameFirst;
            this._nameLast  = object.nameLast;
            this._eMail     = object.eMail;
            this._institute = object.institute;
            this._street    = object.street;
            this._city      = object.city;
            this._country   = object.country;
            this._groups    = object.groups;
            this._roles     = object.roles;
        }
    }
    set nameLogin (nameLogin  : string) { this._nameLogin  = nameLogin; console.log("String is now " + this._nameLogin); }
    set nameFirst (nameFirst  : string) { this._nameFirst  = nameFirst; }
    set nameLast  (nameLast   : string) { this._nameLast   = nameLast;  }
    set eMail     (eMail      : Array<string>) { this._eMail = eMail;   }
    set institute (institute  : string) { this._institute  = institute; }
    set street    (street     : string) { this._street     = street;    }
    set city      (city       : string) { this._city       = city;      }
    set country   (country    : string) { this._country    = country;   }
    set groups    (groups     : string) { this._groups     = groups;    }
    set roles     (roles      : string) { this._roles      = roles;     }
    get nameLogin() : string { return this._nameLogin }
    get nameFirst() : string { return this._nameFirst }
    get nameLast () : string { return this._nameLast  }
    get eMail    () : Array<string> { return this._eMail }
    get institute() : string { return this._institute }
    get street   () : string { return this._street    }
    get city     () : string { return this._city      }
    get country  () : string { return this._country   }
    get groups   () : string { return this._groups    }
    get roles    () : string { return this._roles     }
}