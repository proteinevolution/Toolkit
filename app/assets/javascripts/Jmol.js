/* $RCSfile: Jmol.js,v $
 * $Author: migueljmol $
 * $Date: 2004/12/11 22:22:10 $
 * $Revision: 1.28 $
 *
 * Copyright (C) 2004  The Jmol Development Team
 *
 * Contact: jmol-developers@lists.sf.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 */

// for documentation see www.jmol.org/jslibrary

var undefined; // for IE 5 ... wherein undefined is undefined

////////////////////////////////////////////////////////////////
// Basic Scripting infrastruture
////////////////////////////////////////////////////////////////

function jmolInitialize(codebaseDirectory) {
  if (_jmol.initialized) {
    alert("jmolInitialize() should only be called *ONCE* within a page");
    return;
  }
  if (! codebaseDirectory) {
    alert("codebaseDirectory is a required parameter to jmolInitialize");
    codebaseDirectory = ".";
  }
  if (codebaseDirectory.indexOf("http://") == 0 ||
      codebaseDirectory.indexOf("https://") == 0)
    alert("codebaseDirectory should be directory relative,\n" +
	  "not be an absolute URL : " + codebaseDirectory);
  else if (codebaseDirectory.charAt(0) == '/')
    alert("codebaseDirectory should be directory relative,\n" +
	  "not relative to the root of the web server : " + codebaseDirectory);
  _jmolSetCodebase(codebaseDirectory);
  _jmolOnloadResetForms();
  _jmol.initialized = true;
}

function jmolSetAppletColor(boxbgcolor, boxfgcolor, progresscolor) {
  _jmolInitCheck();
  _jmol.boxbgcolor = boxbgcolor;
  if (boxfgcolor)
    _jmol.boxfgcolor = boxfgcolor
  else if (boxbgcolor == "white" || boxbgcolor == "#FFFFFF")
    _jmol.boxfgcolor = "black";
  else
    _jmol.boxfgcolor = "white";
  if (progresscolor)
    _jmol.progresscolor = progresscolor;
  if (_jmol.debugAlert)
    alert(" boxbgcolor=" + _jmol.boxbgcolor +
          " boxfgcolor=" + _jmol.boxfgcolor +
          " progresscolor=" + _jmol.progresscolor);
}

function jmolApplet(size, script, nameSuffix) {
  _jmolInitCheck();
  _jmolApplet(size, null, script, nameSuffix);
}

////////////////////////////////////////////////////////////////
// Basic controls
////////////////////////////////////////////////////////////////

function jmolButton(script, label, id) {
  _jmolInitCheck();
  var scriptIndex = _jmolAddScript(script);
  if (label == undefined || label == null)
    label = script.substring(0, 32);
  if (id == undefined || id == null)
    id = "jmolButton" + _jmol.buttonCount;
  ++_jmol.buttonCount;
  var t = "<input type='button' name='" + id + "' id='" + id +
          "' value='" + label +
          "' onClick='_jmolClick(" + scriptIndex + _jmol.targetText +
          ")' onMouseover='_jmolMouseOver(" + scriptIndex +
          ");return true' onMouseout='_jmolMouseOut()' " +
          _jmol.buttonCssText + "/>";
  if (_jmol.debugAlert)
    alert(t);
  document.write(t);
}

function jmolCheckbox(scriptWhenChecked, scriptWhenUnchecked,
                      labelHtml, isChecked, id) {
  _jmolInitCheck();
  if (id == undefined || id == null)
    id = "jmolCheckbox" + _jmol.checkboxCount;
  ++_jmol.checkboxCount;
  if (scriptWhenChecked == undefined || scriptWhenChecked == null ||
      scriptWhenUnchecked == undefined || scriptWhenUnchecked == null) {
    alert("jmolCheckbox requires two scripts");
    return;
  }
  if (labelHtml == undefined || labelHtml == null) {
    alert("jmolCheckbox requires a label");
    return;
  }
  var indexChecked = _jmolAddScript(scriptWhenChecked);
  var indexUnchecked = _jmolAddScript(scriptWhenUnchecked);
  var t = "<input type='checkbox' name='" + id + "' id='" + id +
          "' onClick='_jmolCbClick(this," +
          indexChecked + "," + indexUnchecked + _jmol.targetText +
          ")' onMouseover='_jmolCbOver(this," + indexChecked + "," +
          indexUnchecked +
          ");return true' onMouseout='_jmolMouseOut()' " +
	  (isChecked ? "checked " : "") + _jmol.checkboxCssText + "/>" +
          labelHtml;
  if (_jmol.debugAlert)
    alert(t);
  document.write(t);
}

function jmolRadioGroup(arrayOfRadioButtons, separatorHtml, groupName) {
  _jmolInitCheck();
  var type = typeof arrayOfRadioButtons;
  if (type != "object" || type == null || ! arrayOfRadioButtons.length) {
    alert("invalid arrayOfRadioButtons");
    return;
  }
  if (separatorHtml == undefined || separatorHtml == null)
    separatorHtml = "&nbsp; ";
  var length = arrayOfRadioButtons.length;
  var t = "";
  jmolStartNewRadioGroup();
  for (var i = 0; i < length; ++i) {
    var radio = arrayOfRadioButtons[i];
    type = typeof radio;
    if (type == "object") {
      t += _jmolRadio(radio[0], radio[1], radio[2], separatorHtml, groupName);
    } else {
      t += _jmolRadio(radio, null, null, separatorHtml, groupName);
    }
  }
  if (_jmol.debugAlert)
    alert(t);
  document.write(t);
}

function jmolLink(script, text, id) {
  _jmolInitCheck();
  if (id == undefined || id == null)
    id = "jmolLink" + _jmol.linkCount;
  ++_jmol.linkCount;
  var scriptIndex = _jmolAddScript(script);
  var t = "<a name='" + id + "' id='" + id + 
          "' href='javascript:_jmolClick(" + scriptIndex +
          _jmol.targetText +
          ");' onMouseover='_jmolMouseOver(" + scriptIndex +
          ");return true;' onMouseout='_jmolMouseOut()' " +
          _jmol.linkCssText + ">" + text + "</a>";
  if (_jmol.debugAlert)
    alert(t);
  document.write(t);
}

function jmolMenu(arrayOfMenuItems, size, id) {
  _jmolInitCheck();
  if (id == undefined || id == null)
    id = "jmolMenu" + _jmol.menuCount;
  ++_jmol.menuCount;
  var type = typeof arrayOfMenuItems;
  if (type != null && type == "object" && arrayOfMenuItems.length) {
    var length = arrayOfMenuItems.length;
    if (typeof size != "number" || size == 1)
      size = null;
    else if (size < 0)
      size = length;
    var sizeText = size ? " size='" + size + "' " : "";
    var t = "<select name='" + id + "' id='" + id +
            "' onChange='_jmolMenuSelected(this" +
            _jmol.targetText + ")'" +
            sizeText + _jmol.menuCssText + ">";
    for (var i = 0; i < length; ++i) {
      var menuItem = arrayOfMenuItems[i];
      type = typeof menuItem;
      var script, text;
      var isSelected = undefined;
      if (type == "object" && menuItem != null) {
        script = menuItem[0];
        text = menuItem[1];
        isSelected = menuItem[2];
      } else {
        script = text = menuItem;
      }
      if (text == undefined || text == null)
        text = script;
      var scriptIndex = _jmolAddScript(script);
      var selectedText = isSelected ? "' selected>" : "'>";
      t += "<option value='" + scriptIndex + selectedText + text + "</option>";
    }
    t += "</select>";
    if (_jmol.debugAlert)
      alert(t);
    document.write(t);
  }
}

function jmolHtml(html) {
  document.write(html);
}

function jmolBr() {
  document.write("<br />");
}

////////////////////////////////////////////////////////////////
// advanced scripting functions
////////////////////////////////////////////////////////////////

function jmolDebugAlert(enableAlerts) {
  _jmol.debugAlert = (enableAlerts == undefined || enableAlerts)
}

function jmolAppletInline(size, inlineModel, script, nameSuffix) {
  _jmolApplet(size, _jmolConvertInline(inlineModel), script, nameSuffix);
}

function jmolSetTarget(targetSuffix) {
  _jmol.targetSuffix = targetSuffix;
  _jmol.targetText = targetSuffix ? ",\"" + targetSuffix + "\"" : "";
}

function jmolScript(script, targetSuffix) {
  if (script) {
    _jmolCheckBrowser();
    var target = "jmolApplet" + (targetSuffix ? targetSuffix : "0");
    var applet = _jmolFindApplet(target);
    if (applet)
      return applet.script(script);
    else
      alert("could not find applet " + target);
  }
}

function jmolLoadInline(model, targetSuffix) {
  if (model) {
    var target = "jmolApplet" + (targetSuffix ? targetSuffix : "0");
//    while (! _jmol.ready[target])
//      alert("The Jmol applet " + target + " is not loaded yet");
//    if (! _jmol.ready[target])
//      alert("The Jmol applet " + target + " is not loaded yet");
//    if (document.applets[target] && ! document.applets[target].isActive())
//       alert("The Jmol applet " + target + " is not yet active");
//    else {
      var applet = _jmolFindApplet(target);
      if (applet)
        return applet.loadInline(model);
      else
        alert("could not find applet " + target);
//    }
  }
}

function jmolStartNewRadioGroup() {
  ++_jmol.radioGroupCount;
}

function jmolRadio(script, labelHtml, isChecked, separatorHtml, groupName) {
  _jmolInitCheck();
  if (_jmol.radioGroupCount == 0)
    ++_jmol.radioGroupCount;
  var t = _jmolRadio(script, labelHtml, isChecked, separatorHtml, groupName);
  if (_jmol.debugAlert)
    alert(t);
  document.write(t);
}

function jmolCheckBrowser(action, urlOrMessage, nowOrLater) {
  if (typeof action == "string") {
    action = action.toLowerCase();
    if (action != "alert" && action != "redirect" && action != "popup")
      action = null;
  }
  if (typeof action != "string")
    alert("jmolCheckBrowser(action, urlOrMessage, nowOrLater)\n\n" +
          "action must be 'alert', 'redirect', or 'popup'");
  else {
    if (typeof urlOrMessage != "string")
      alert("jmolCheckBrowser(action, urlOrMessage, nowOrLater)\n\n" +
            "urlOrMessage must be a string");
    else {
      _jmol.checkBrowserAction = action;
      _jmol.checkBrowserUrlOrMessage = urlOrMessage;
    }
  }
  if (typeof nowOrLater == "string" && nowOrLater.toLowerCase() == "now")
    _jmolCheckBrowser();
}

////////////////////////////////////////////////////////////////
// Cascading Style Sheet Class support
////////////////////////////////////////////////////////////////

function jmolSetAppletCssClass(appletCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.appletCssClass = appletCssClass;
    _jmol.appletCssText = appletCssClass ? "class='" + appletCssClass + "' " : "";
  }
}

function jmolSetButtonCssClass(buttonCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.buttonCssClass = buttonCssClass;
    _jmol.buttonCssText = buttonCssClass ? "class='" + buttonCssClass + "' " : "";
  }
}

function jmolSetCheckboxCssClass(checkboxCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.checkboxCssClass = checkboxCssClass;
    _jmol.checkboxCssText = checkboxCssClass ? "class='" + checkboxCssClass + "' " : "";
  }
}

function jmolSetRadioCssClass(radioCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.radioCssClass = radioCssClass;
    _jmol.radioCssText = radioCssClass ? "class='" + radioCssClass + "' " : "";
  }
}

function jmolSetLinkCssClass(linkCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.linkCssClass = linkCssClass;
    _jmol.linkCssText = linkCssClass ? "class='" + linkCssClass + "' " : "";
  }
}

function jmolSetMenuCssClass(menuCssClass) {
  if (_jmol.hasGetElementById) {
    _jmol.menuCssClass = menuCssClass;
    _jmol.menuCssText = menuCssClass ? "class='" + menuCssClass + "' " : "";
  }
}

////////////////////////////////////////////////////////////////
// functions for INTERNAL USE ONLY which are subject to change
// use at your own risk ... you have been WARNED!
////////////////////////////////////////////////////////////////

var _jmol = {

debugAlert: false,
bgcolor: "black",
progresscolor: "blue",
boxbgcolor: "black",
boxfgcolor: "white",
boxmessage: "Downloading JmolApplet ...",

codebase: ".",
modelbase: ".",

appletCount: 0,

buttonCount: 0,
checkboxCount: 0,
linkCount: 0,
menuCount: 0,
radioCount: 0,
radioGroupCount: 0,

appletCssClass: null,
appletCssText: "",
buttonCssClass: null,
buttonCssText: "",
checkboxCssClass: null,
checkboxCssText: "",
radioCssClass: null,
radioCssText: "",
linkCssClass: null,
linkCssText: "",
menuCssClass: null,
menuCssText: "",

targetSuffix: 0,
targetText: "",
scripts: [""],

ua: navigator.userAgent.toLowerCase(),
uaVersion: parseFloat(navigator.appVersion),

os: "unknown",
browser: "unknown",
browserVersion: 0,
hasGetElementById: !!document.getElementById,
isJavaEnabled: navigator.javaEnabled(),
isNetscape47Win: false,

isBrowserCompliant: false,
isJavaCompliant: false,
isFullyCompliant: false,

initialized: false,
initChecked: false,

browserChecked: false,
checkBrowserAction: "alert",
checkBrowserUrlOrMessage: null,

previousOnloadHandler: null,
ready: {}
}

with (_jmol) {
  function _jmolTestUA(candidate) {
    var ua = _jmol.ua;
    var index = ua.indexOf(candidate);
    if (index < 0)
      return false;
    _jmol.browser = candidate;
    _jmol.browserVersion = parseFloat(ua.substring(index + candidate.length+1));
    return true;
  }
  
  function _jmolTestOS(candidate) {
    if (_jmol.ua.indexOf(candidate) < 0)
      return false;
    _jmol.os = candidate;
    return true;
  }
  
  _jmolTestUA("konqueror") ||
  _jmolTestUA("safari") ||
  _jmolTestUA("omniweb") ||
  _jmolTestUA("opera") ||
  _jmolTestUA("webtv") ||
  _jmolTestUA("icab") ||
  _jmolTestUA("msie") ||
  (_jmol.ua.indexOf("compatible") < 0 && _jmolTestUA("mozilla"));
  
  _jmolTestOS("linux") ||
  _jmolTestOS("unix") ||
  _jmolTestOS("mac") ||
  _jmolTestOS("win");

  isNetscape47Win = (os == "win" && browser == "mozilla" &&
                     browserVersion >= 4.78 && browserVersion <= 4.8);

  if (os == "win") {
    isBrowserCompliant = hasGetElementById || isNetscape47Win;
  } else if (os == "mac") { // mac is the problem child :-(
    if (browser == "mozilla" && browserVersion >= 5) {
      // miguel 2004 11 17
      // checking the plugins array does not work because
      // Netscape 7.2 OS X still has Java 1.3.1 listed even though
      // javaplugin.sf.net is installed to upgrade to 1.4.2
      eval("try {var v = java.lang.System.getProperty('java.version');" +
           " _jmol.isBrowserCompliant = v >= '1.4.2';" +
           " } catch (e) { }");
    } else if (browser == "opera" && browserVersion <= 7.54) {
      isBrowserCompliant = false;
    } else {
      isBrowserCompliant = hasGetElementById &&
        !((browser == "msie") ||
          (browser == "safari" && browserVersion < 125.1));
    }
  } else if (os == "linux" || os == "unix") {
    if (browser == "konqueror" && browserVersion <= 3.3)
      isBrowserCompliant = false;
    else
      isBrowserCompliant = hasGetElementById;
  } else { // other OS
    isBrowserCompliant = hasGetElementById;
  }

  // possibly more checks in the future for this
  isJavaCompliant = isJavaEnabled;

  isFullyCompliant = isBrowserCompliant && isJavaCompliant;
}

function _jmolApplet(size, inlineModel, script, nameSuffix) {
  with (_jmol) {
    if (! nameSuffix)
      nameSuffix = appletCount;
    ++appletCount;
    if (! script)
      script = "select *";
    var sz = _jmolGetAppletSize(size);
    var t;
    t = "<applet name='jmolApplet" + nameSuffix + "' id='jmolApplet" + nameSuffix +
        "' " + appletCssText +
        " code='JmolApplet' archive='JmolApplet.jar'\n" +
        " codebase='" + codebase + "'\n" +
        " width='" + sz[0] + "' height='" + sz[1] +
        "' mayscript='true'>\n" +
        "  <param name='progressbar' value='true' />\n" +
        "  <param name='progresscolor' value='" +
        progresscolor + "' />\n" +
        "  <param name='boxmessage' value='" +
        boxmessage + "' />\n" +
        "  <param name='boxbgcolor' value='" +
        boxbgcolor + "' />\n" +
        "  <param name='boxfgcolor' value='" +
        boxfgcolor + "' />\n" +
        "  <param name='ReadyCallback' value='_jmolReadyCallback' />\n";

    if (inlineModel)
      t += "  <param name='loadInline' value='" + inlineModel + "' />\n";
    if (script)
      t += "  <param name='script' value='" + script + "' />\n";
    t += "</applet>";
    jmolSetTarget(nameSuffix);
    ready["jmolApplet" + nameSuffix] = false;
    if (_jmol.debugAlert)
      alert(t);
    document.write(t);
  }
}

function _jmolInitCheck() {
  if (_jmol.initChecked)
    return;
  _jmol.initChecked = true;
  if (_jmol.initialized)
    return;
  alert("jmolInitialize({codebase}, {badBrowseURL}, {badJavaURL})\n" +
        "  must be called before any other Jmol.js functions");
}

function _jmolCheckBrowser() {
  with (_jmol) {
    if (browserChecked)
      return;
    browserChecked = true;
  
    if (isFullyCompliant)
      return true;

    if (checkBrowserAction == "redirect")
      location.href = checkBrowserUrlOrMessage;
    else if (checkBrowserAction == "popup")
      _jmolPopup(checkBrowserUrlOrMessage);
    else {
      var msg = checkBrowserUrlOrMessage;
      if (msg == null)
        msg = "Your web browser is not fully compatible with Jmol\n\n" +
              "brower: " + browser +
              "   version: " + browserVersion +
              "   os: " + os +
              "\n\n" + ua;
      alert(msg);
    }
  }
  return false;
}

function _jmolPopup(url) {
  var popup = window.open(url, "JmolPopup",
                          "left=150,top=150,height=400,width=600," +
                          "directories=yes,location=yes,menubar=yes," +
                          "toolbar=yes," +
                          "resizable=yes,scrollbars=yes,status=yes");
  if (popup.focus)
    poup.focus();
}

function _jmolReadyCallback(name) {
  if (_jmol.debugAlert)
    alert(name + " is ready");
  _jmol.ready["" + name] = true;
}

function _jmolConvertInline(model) {
  var inlineModel = model.replace(/\r|\n|\r\n/g, "|");
  if (_jmol.debugAlert)
    alert("inline model:\n" + inlineModel);
  return inlineModel;
}

function _jmolGetAppletSize(size) {
  var width, height;
  var type = typeof size;
  if (type == "number")
    width = height = size;
  else if (type == "object" && size != null) {
    width = size[0]; height = size[1];
  }
  if (! (width >= 25 && width <= 2000))
    width = 300;
  if (! (height >= 25 && height <= 2000))
    height = 300;
  return [width, height];
}

function _jmolRadio(script, labelHtml, isChecked, separatorHtml, groupName) {
  ++_jmol.radioCount;
  if (groupName == undefined || groupName == null)
    groupName = "jmolRadioGroup" + (_jmol.radioGroupCount - 1);
  if (!script)
    return "";
  if (labelHtml == undefined || labelHtml == null)
    labelHtml = script.substring(0, 32);
  if (! separatorHtml)
    separatorHtml = "";
  var scriptIndex = _jmolAddScript(script);
  return "<input name='" + groupName +
         "' type='radio' onClick='_jmolClick(" +
         scriptIndex + _jmol.targetText +
         ");return true;' onMouseover='_jmolMouseOver(" +
         scriptIndex +
         ");return true;' onMouseout='_jmolMouseOut()' " +
	 (isChecked ? "checked " : "") + _jmol.radioCssText + "/>" +
         labelHtml + separatorHtml;
}

function _jmolFindApplet(target) {
  // first look for the target in the current window
  var applet = _jmolSearchFrames(window, target);
  if (applet == undefined)
    applet = _jmolSearchFrames(top, target); // look starting in top frame
  return applet;
}

function _jmolSearchFrames(win, target) {
  var applet;
  var frames = win.frames;
  if (frames && frames.length) { // look in all the frames below this window
    for (var i = 0; i < frames.length; ++i) {
      applet = _jmolSearchFrames(frames[i++], target);
      if (applet)
        break;
    }
  } else { // look for the applet in this window
    var doc = win.document;
// getElementById fails on MacOSX Safari & Mozilla	
    if (doc.applets)
      applet = doc.applets[target];
    else
      applet = doc[target];
  }
  return applet;
}

function _jmolAddScript(script) {
  if (! script)
    return 0;
  var index = _jmol.scripts.length;
  _jmol.scripts[index] = script;
  return index;
}

function _jmolClick(scriptIndex, targetSuffix) {
  jmolScript(_jmol.scripts[scriptIndex], targetSuffix);
}

function _jmolMenuSelected(menuObject, targetSuffix) {
  var scriptIndex = menuObject.value;
  if (scriptIndex != undefined) {
    jmolScript(_jmol.scripts[scriptIndex], targetSuffix);
    return;
  }
  var length = menuObject.length;
  if (typeof length == "number") {
    for (var i = 0; i < length; ++i) {
      if (menuObject[i].selected) {
        _jmolClick(menuObject[i].value, targetSuffix);
	return;
      }
    }
  }
  alert("?Que? menu selected bug #8734");
}

function _jmolCbClick(ckbox, whenChecked, whenUnchecked, targetSuffix) {
  _jmolClick(ckbox.checked ? whenChecked : whenUnchecked, targetSuffix);
}

function _jmolCbOver(ckbox, whenChecked, whenUnchecked) {
  window.status = _jmol.scripts[ckbox.checked ? whenUnchecked : whenChecked];
}

function _jmolMouseOver(scriptIndex) {
  window.status = _jmol.scripts[scriptIndex];
}

function _jmolMouseOut() {
  window.status = " ";
  return true;
}

function _jmolSetCodebase(codebase) {
  _jmol.codebase = codebase ? codebase : ".";
  if (_jmol.debugAlert)
    alert("jmolCodebase=" + _jmol.codebase);
}

function _jmolOnloadResetForms() {
  _jmol.previousOnloadHandler = window.onload;
  window.onload =
  function() {
//    alert("onloadResetForms");
    with (_jmol) {
      if (buttonCount+checkboxCount+menuCount+radioCount+radioGroupCount > 0) {
        var forms = document.forms;
        if (!forms || forms.length == 0) {
          alert("<form> tags seem to be missing\n" +
                "Jmol/HTML input controls must be contained " +
                "within form tags"
//                + "\n\n" + forms + " forms.length=" + forms.length +
//                " typeof=" + (typeof forms)
                );
        } else {
          for (var i = forms.length; --i >= 0; )
            forms[i].reset();
        }
      }
      if (previousOnloadHandler)
        previousOnloadHandler();
    }
  }
}

