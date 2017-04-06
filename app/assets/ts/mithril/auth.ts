let jqueryUITabsInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).tabs();
    }
};
//let foundationDropdownInit = function(elem : Element, isInit : boolean) : void {
//    if (!isInit) {
//        Foundation.Dropdown(elem);
//    }
//};

class SignIn {
    static nameLogin : string = "";
    static password  : string = "";
    static nameLoginSetter (nameLogin : string) : void { SignIn.nameLogin = nameLogin; redrawDelay(); }
    static passwordSetter  (password  : string) : void { SignIn.password  = password;  redrawDelay(); }

    static submit(event : Event) : void {
        event.preventDefault();
        var dataS = {nameLogin:SignIn.nameLogin, password:SignIn.password};
        m.request({method: "POST", url: "signin", data: dataS }).then(function(authMessage) {
            //type : AuthMessage
            if (authMessage.successful) { SignIn.password = null }
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
    static nameLoginSetter(nameLogin  : string)  : void { SignUp.nameLogin = nameLogin; redrawDelay(); }
    static eMailSetter    (eMail      : string)  : void { SignUp.eMail     = eMail;     redrawDelay(); }
    static passwordSetter (password   : string)  : void { SignUp.password  = password;  redrawDelay(); }
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
                                 type:           'password'
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
    static nameLoginSetter (nameLogin  : string) : void { ForgotPassword.nameLogin = nameLogin; redrawDelay(); }
    static eMailSetter     (eMail      : string) : void { ForgotPassword.eMail     = eMail;     redrawDelay(); }

    static submit(event : Event) : void {
        event.preventDefault();
        var dataS = {nameLogin:ForgotPassword.nameLogin, eMail:ForgotPassword.eMail};
        console.log(dataS);
        m.request({method: "POST", url: "forgotPassword", data: dataS }).then(function(authMessage) {
            //type : AuthMessage
            if (authMessage.successful) { SignIn.password = null }
            Auth.updateStatus(authMessage);
        });
    }

    static controller (args : any) : any {
        return {}
    }
    static view (ctrl : any, args : any) : any {
        return m("div", { class : "auth-form" },
            m("form", { 'data-abide': 'ajax', id: 'signup-form', novalidate:'novalidate', onsubmit: ForgotPassword.submit }, [
                m("div", m("label",
                    m("input", { id:         'nameLogin',
                                 name:       'nameLogin',
                                 pattern:    '[a-zA-Z0-9_]{6,40}',
                                 placeholder:'Username',
                                 type:       'text',
                                 onkeyup:    m.withAttr("value", ForgotPassword.nameLoginSetter),
                                 onchange:   m.withAttr("value", ForgotPassword.nameLoginSetter),
                                 value:      ForgotPassword.nameLogin
                    })
                )),
                m("div", m("label",
                    m("input", { id:          'eMail',
                                 name:        'eMail',
                                 pattern:     'email',
                                 placeholder: 'E-Mail',
                                 type:        'text',
                                 onkeyup:     m.withAttr("value", ForgotPassword.eMailSetter),
                                 onchange:    m.withAttr("value", ForgotPassword.eMailSetter),
                                 value:       ForgotPassword.eMail
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
    static eMail     : string = "";
    static eMailF(eMail : string) : string {
        if (eMail) SignUp.eMail = eMail;
        return SignUp.eMail
    }

    static controller (args : any) : any {
        if (args) {
            SignUp.eMail     = args.eMail     ? args.eMail     : SignUp.eMail;
        }
        return { eMail : SignUp.eMail }
    }

    static view (ctrl : any, args : any) : any {
        if (Auth.user == null) {
        return m("div")
        } else {
        return m("div", {class: "auth-form"},
            m("form", { 'data-abide': 'ajax', id:'profile-edit-form' }, [
                m("div", m("label", [
                    m("input", { id: 'nameFirst',
                                 name:       'nameFirst',
                                 pattern:    '[a-zA-Z0-9_]{6,40}',
                                 placeholder:'Username',
                                 required:   'required',
                                 type:       'text',
                                 value:       ctrl.user ? ctrl.user.nameFirst : ""
                    }),
                    m("span", {class:"form-error"}, "Username must be at least 6 characters long!")
                ])),
                m("div", m("label", [
                    m("input", { id:         'passwordCheck',
                                 pattern:    '.{8,128}',
                                 placeholder:'Password',
                                 required:   'required',
                                 type:       'password',
                                 onKeyUp:     m.withAttr("value", SignUp.passwordSetter),
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
                                 type:           'password'
                    }),
                    m("span", {class:"form-error"}, "Passwords must match!")
                ])),
                m("div", m("label", [
                    m("input", { id:          'eMail',
                                 name:        'eMail',
                                 pattern:     'email',
                                 placeholder: 'E-Mail',
                                 type:        'text',
                                 onKeyUp:     m.withAttr("value", SignUp.eMailSetter),
                                 value:       SignUp.eMail
                    }),
                    m("span", {class:"form-error"}, "Please enter a valid e-Mail address!")
                ])),
                m("div", m("label", {id:'checklabel'}, [
                    m("input", { id:       'acceptToS',
                                 name:     'acceptToS',
                                 required: 'required',
                                 type:     'checkbox',
                                 value:    'false'
                    }),
                    "I Accept the Terms of Sevice",
                    m("span", {class:"form-error",id:'acceptToSText'}, "You must accept the ToS!")
                ])),
                m("input", { class: "input small expanded secondary button disabled",
                             id:    'signup-submit',
                             type:  'submit',
                             value: 'Register'
                })
            ])
        )
        }
    }
}


class AuthDropdown {
    static controller (args : any) : any {
        if (args) {
        }
        return {}
    }
    static view (ctrl : any, args : any) : any {
        if (Auth.user == null) {
            return m("button", { id:"auth-link", onclick: openNav}, "Sign in")
        } else {
            return m("div", { },
                    m("ul", { id:    "auth-dropdown-link",
                              class: "dropdown menu",
                              'data-dropdown-menu': 'data-dropdown-menu',
                              'data-alignment':     "right",
                              'data-disable-hover': true,
                              'data-click-open':    true
                    }, m("li", [
                        m("button", { class : "loggedIn", id: "auth-link-text"}, Auth.user.nameLogin),
                        m("ul", { class :"menu noFouc" }, [
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

class LoginTabs {
    static controller (args : any) : any {
        if (args) {
        }
        return {}
    }

    static view (ctrl : any, args : any) : any {
        return m("div", {id:"auth-tabs", config: jqueryUITabsInit}, [
            m("ul", {id:"auth-tab"}, [
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
        return m("div", {id:"auth-tabs", config: jqueryUITabsInit}, [
            m("ul", {id:"auth-tab"}, [
                m("li", m("a", {href:"#auth-tab-edit"}, "Edit Profile")),
                m("li", m("a", {href:"#auth-tab-password"}, "Change Password")),
                m("li", m("a", {href:"#auth-tab-user"}, Auth.user.nameLogin))
            ]),
            Auth.message == null ? null :
                !Auth.message.successful ?
                    m("div", {class:"callout alert", id:"auth-alert", onclick:Auth.resetStatus()}, Auth.message.message) :
                    m("div", {class:"callout",       id:"auth-alert", onclick:Auth.resetStatus()}, Auth.message.message),
            m("div", {class:"tabs-panel", id:"auth-tab-edit"},     m.component(SignIn, {})),
            m("div", {class:"tabs-panel", id:"auth-tab-password"}, m.component(SignUp, {})),
            m("div", {class:"tabs-panel", id:"auth-tab-user"},     m.component(ForgotPassword, {}))
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
    static loadUser () : void {
        console.log("Requesting userdata")
        m.request({method: "GET", url: "userData" }).then(function(data) {
            //type : AuthMessage
            console.log("user: ", data);
            if (data) {
                SignIn.password = null;
                Auth.user       = data.user != null ? data.user : null;
            }
        });
    }
    static updateStatus(authMessage : AuthMessage) : void {
        console.log("updating status!", authMessage);
        Auth.message = authMessage;
        Auth.user = authMessage.successful ? authMessage.user : null;
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
    constructor(private _nameLogin  : string = "invalid",
                private _eMail      : string = "invalid",
                private _nameFirst  : string = null,
                private _nameLast   : string = null,
                private _institute  : string = null,
                private _street     : string = null,
                private _city       : string = null,
                private _country    : string = null,
                private _groups     : string = null,
                private _roles      : string = null){}
    set nameLogin (nameLogin  : string) { this._nameLogin  = nameLogin  }
    set nameFirst (nameFirst  : string) { this._nameFirst  = nameFirst  }
    set nameLast  (nameLast   : string) { this._nameLast   = nameLast   }
    set eMail     (eMail      : string) { this._eMail      = eMail      }
    set institute (institute  : string) { this._institute  = institute  }
    set street    (street     : string) { this._street     = street     }
    set city      (city       : string) { this._city       = city       }
    set country   (country    : string) { this._country    = country    }
    set groups    (groups     : string) { this._groups     = groups     }
    set roles     (roles      : string) { this._roles      = roles      }
    get nameLogin() : string { return this._nameLogin }
    get nameFirst() : string { return this._nameFirst }
    get nameLast () : string { return this._nameLast  }
    get eMail    () : string { return this._eMail     }
    get institute() : string { return this._institute }
    get street   () : string { return this._street    }
    get city     () : string { return this._city      }
    get country  () : string { return this._country   }
    get groups   () : string { return this._groups    }
    get roles    () : string { return this._roles     }
}