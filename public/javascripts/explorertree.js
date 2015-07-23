/*
Explorer Tree 1.8
=================
by Andrew Gregory
http://www.scss.com.au/family/andrew/webdesign/explorertree/

This work is licensed under the Creative Commons Attribution License. To view a
copy of this license, visit http://creativecommons.org/licenses/by/1.0/ or send
a letter to Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305,
USA.

IMPORTANT NOTE:
Variables and functions with names starting with an underscore (_) are
'internal' and not to be used.

IMPORTANT : cssutil.js and events.js are intergrated into the main explorertree.js

*/


/*
CSS Utilities
by Andrew Gregory
http://www.scss.com.au/family/andrew/

I have placed this code in the public domain. Feel free to use it however you
wish.

v1.3  6-Oct-2004 Added el.className checks
v1.2  5-Aug-2004 Simplified code by using regular expressions.
v1.1 12-Apr-2004 Fixed bug in elementRemoveClass() which removed partially matching classnames.
v1.0 29-Mar-2004 Initial version. Allows non-destructive setting and removal of CSS class names.
*/
// Test if an element has the given CSS class
function elementHasClass(el,cl){return (el.className&&el.className.search(new RegExp('\\b'+cl+'\\b'))>-1);}
// Ensure an element has the given CSS class
function elementAddClass(el,cl){var c=el.className;if(!c)c='';if(!elementHasClass(el,cl))c+=((c.length>0)?' ':'')+cl;el.className=c;}
// Ensure an element no longer has the given CSS class 
function elementRemoveClass(el,cl){if(el.className)el.className=el.className.replace(new RegExp('\\s*\\b'+cl+'\\b\\s*'),' ').replace(/^\s*/,'').replace(/\s*$/,'');}


// Cross-browser event handling
// by Scott Andrew LePera
// http://www.scottandrew.com/weblog/articles/cbs-events

// Modified 2004-08-10 by Andrew Grgeory to work around Konqueror bug
// Modified 2004-06-04 by Andrew Gregory to support legacy (NS3,4) browsers
// http://www.scss.com.au/family/andrew/

// eg. addEvent(imgObj, 'mousedown', processEvent, false);
function addEvent(obj, evType, fn, useCapture) {
  // work around Konqueror bug #57913 which prevents
  // window.addEventListener('load',...) from working
  var ua = navigator.userAgent;
  var konq = ua.indexOf('KHTML') != -1 && ua.indexOf('Safari') == -1 && obj == window && evType == 'load';
  // don't use addEventListener for Konq, have Konq fall back to the old
  // obj.onload method
  if (obj.addEventListener && !konq) {
    obj.addEventListener(evType, fn, useCapture);
    return true;
  } else if (obj.attachEvent) {
    return obj.attachEvent('on' + evType, fn);
  } else {
    if (!obj.cb_events) {
      obj.cb_events = new Object();
      obj.cb_ftemp = null;
    }
    var events = obj.cb_events[evType];
    if (!events) {
      events = new Array();
      obj.cb_events[evType] = events;
    }
    var i = 0;
    while ((i < events.length) && (events[i] != fn)) {
      i++;
    }
    if (i == events.length) {
      events[i] = fn;
      obj['on' + evType] = new Function("var ret=false,e=this.cb_events['"+evType+"'];if(e){for(var i=0;i<e.length;i++){this.cb_ftemp=e[i];ret=this.cb_ftemp()||ret;}return ret;}");
    }
    return true;
  }
}

// eg. removeEvent(imgObj, 'mousedown', processEvent, false);
function removeEvent(obj, evType, fn, useCapture) {
  // work around Konqueror bug #57913 which prevents
  // window.addEventListener('load',...) from working
  var ua = navigator.userAgent;
  var konq = ua.indexOf('KHTML') != -1 && ua.indexOf('Safari') == -1 && obj == window && evType == 'load';
  // don't use addEventListener for Konq, have Konq fall back to the old
  // obj.onload method
  if (obj.removeEventListener && !konq) {
    obj.removeEventListener(evType, fn, useCapture);
    return true;
  } else if (obj.detachEvent) {
    return obj.detachEvent('on' + evType, fn);
  } else {
    var ret = false;
    if (obj.cb_events) {
      var events = obj.cb_events[evType];
      if (events) {
        // remove any matching functions from the events array, shuffling items
        // down to fill in the space before truncating the array
        var dest = 0;
        for (var src = 0; src < events.length; src++) {
          if (dest != src) {
            events[dest] = events[src];
          }
          if (events[dest] == fn) {
            ret = true;
          } else {
            dest++;
          }
        }
        events.length = dest;
      }
    }
    return ret;
  }
}


var explorerTreeAutoCollapse = {'default':false};
var explorerTreeBulletWidth = {'default':20};
var explorerTreeIE7Supported = true; // allow style changes to occur if IE7 is in use
var explorerTreeIE7Recalc = true; // recalculate the document at appropriate points if IE7 is in use

function explorerTreeDocRecalc() {
  if (window.IE7 &&
      explorerTreeIE7Supported &&
      (typeof document.recalc == 'function') &&
      explorerTreeIE7Recalc) document.recalc();
}

// Refresh all explorer trees
function explorerTreeRefreshAll() {
  // We don't actually need createElement, but we do
  // need good DOM support, so this is a good check.
  //if (!document.createElement) return;
  
  var ul, uls = document.getElementsByTagName('ul');
  for (var uli = 0; uli < uls.length; uli++) {
    ul = uls[uli];
    if (ul.nodeName.toLowerCase() == 'ul' && elementHasClass(ul, 'explorertree')) {
      _explorerTreeInitUL(ul);
    }
  }
  if (uls.length > 0) explorerTreeDocRecalc();
}

// Refresh the specified explorer tree
function explorerTreeRefresh(id) {
  _explorerTreeInitUL(document.getElementById(id));
  explorerTreeDocRecalc();
}

// Get the root element (<ul>) of the tree the given element is part of.
function _explorerTreeGetRoot(element) {
  for (var e = element; e != null; e = e.parentNode) {
    if (e.nodeName.toLowerCase() == 'ul' && elementHasClass(e, 'explorertree')) {
      break;
    }
  }
  return e;
}

// Get the ID of the tree the given element is part of. Returns the ID or
// 'default' if there is no ID.
function _explorerTreeGetId(element) {
  var e = _explorerTreeGetRoot(element);
  var id = e ? e.getAttribute('id') : '';
  return (!id || id == '') ? 'default' : id;
}

// Initialise the given list
function _explorerTreeInitUL(ul) {
  if (window.IE7 && !explorerTreeIE7Supported) return;
  if (navigator.userAgent.indexOf('Gecko') != -1) {
    addEvent(ul, 'mousedown', _explorerTreeStopGeckoSelect, false);
  }
  if (!ul.childNodes || ul.childNodes.length == 0) return;
  // Iterate LIs
  for (var itemi = 0; itemi < ul.childNodes.length; itemi++) {
    var item = ul.childNodes[itemi];
    if (item.nodeName.toLowerCase() == 'li') {
      addEvent(item, 'click', _explorerTreeOnClick, false);
      // Iterate things in this LI
      var hassubul = false;
      for (var subitemi = 0; subitemi < item.childNodes.length; subitemi++) {
        var subitem = item.childNodes[subitemi];
        if (subitem.nodeName.toLowerCase() == 'a') {
          addEvent(subitem, 'click', _explorerTreeOnClick, false);
        }
        if (subitem.nodeName.toLowerCase() == 'ul') {
          hassubul = true; 
          _explorerTreeInitUL(subitem);
        }
      }
      if (hassubul) {
        // item is expandable, but don't change it if it's already been set to
        // something else
        if (!elementHasClass(item, 'explorertree-open') &&
            !elementHasClass(item, 'explorertree-bullet')) {
          elementAddClass(item, 'explorertree-closed');
        }
      } else {
        // item has no sub-lists, make sure it's non-expandable
        elementRemoveClass(item, 'explorertree-open');
        elementRemoveClass(item, 'explorertree-closed');
        elementAddClass(item, 'explorertree-bullet');
      }
    }
  }
}

// Gecko selects text when bullets are clicked on - stop it!
function _explorerTreeStopGeckoSelect(evt) {
  if (!evt) var evt = window.event;
  if (evt.preventDefault) {
    evt.preventDefault();
  }
  return true;
}

// Handle clicking on LI and A elements in the tree.
function _explorerTreeOnClick(evt) {
  if (!evt) var evt = window.event;
  var element = (evt.target) ? evt.target : evt.srcElement;
  if (this != element) {
    return true;
  }
  if (element.nodeName.toLowerCase() == 'li') {
    // toggle open/closed state, if possible
    if (elementHasClass(element, 'explorertree-open')) {
      elementRemoveClass(element, 'explorertree-open');
      elementAddClass(element, 'explorertree-closed');
    } else if (elementHasClass(element, 'explorertree-closed')) {
      elementRemoveClass(element, 'explorertree-closed');
      elementAddClass(element, 'explorertree-open');
    } else {
      return true;
    }
    if (explorerTreeAutoCollapse[_explorerTreeGetId(element)]) {
      _explorerTreeCollapseAllButElement(element);
    }
    explorerTreeDocRecalc();
  } else if (element.nodeName.toLowerCase() == 'a') {
    // let hyperlinks work as expected
    // TO DO: target support untested!!!
    var href = element.getAttribute('href');
    if (href) {
      var target = element.getAttribute('target');
      if (!target) {
        target = '_self';
      }
      switch (target) {
        case '_blank':
          window.open(href);
          break;
        case '_self':
          window.location.href = href;
          break;
        case '_parent':
          window.parent.location.href = href;
          break;
        case '_top':
          window.top.location.href = href;
          break;
        default:
          window.open(href, target);
          break;
      }
    }
  } else {
    return true;
  }
  // we handled the event - stop any default actions
  evt.returnValue = false;
  if (evt.preventDefault) {
    evt.preventDefault();
  }
  return false;
}

// Open the specified tree branch
function _explorerTreeOpen(li) {
  if (!elementHasClass(li, 'explorertree-bullet')) {
    elementRemoveClass(li, 'explorertree-closed');
    elementAddClass(li, 'explorertree-open');
  }
}

// Close the specified tree branch
function _explorerTreeClose(li) {
  if (!elementHasClass(li, 'explorertree-bullet')) {
    elementRemoveClass(li, 'explorertree-open');
    elementAddClass(li, 'explorertree-closed');
  }
}

// Collapse the specified tree
function explorerTreeCollapse(id) {
  _explorerTreeSetState(document.getElementById(id), true, null);
  explorerTreeDocRecalc();
}

// Fully expand the specified tree
function explorerTreeExpand(id) {
  if (!explorerTreeAutoCollapse[id]) {
    _explorerTreeSetState(document.getElementById(id), false, null);
    explorerTreeDocRecalc();
  }
}

// Collapse all the branches of tree except for those leading to the specified
// element. 
function _explorerTreeCollapseAllButElement(e) {
  var excluded = new Array();
  var tree = null;
  for (var element = e; element != null; element = element.parentNode) {
    if (element.nodeName.toLowerCase() == 'li') {
      excluded[excluded.length] = element;
    }
    if (element.nodeName.toLowerCase() == 'ul' && elementHasClass(element, 'explorertree')) {
      tree = element;
    }
  }
  if (tree) {
    _explorerTreeSetState(tree, true, excluded)
  }
}

// Set the open/closed state of all the LIs under the tree.
// The excludedElements parameter is used to implement the auto-collapse feature
// that automatically collapses tree branches other than the one actively being
// opened by the user.
function _explorerTreeSetState(ul, collapse, excludedElements) {
  if (window.IE7 && !explorerTreeIE7Supported) return;
  if (!ul.childNodes || ul.childNodes.length == 0) return;
  // Iterate LIs
  for (var itemi = 0; itemi < ul.childNodes.length; itemi++) {
    var item = ul.childNodes[itemi];
    if (item.nodeName.toLowerCase() == 'li') {
      var excluded = false;
      if (excludedElements) {
        for (var exi = 0; exi < excludedElements.length; exi++) {
          if (item == excludedElements[exi]) {
            excluded = true;
            break;
          }
        }
      }
      if (!excluded) {
        if (collapse) {
          _explorerTreeClose(item);
        } else {
          _explorerTreeOpen(item);
        }
      }
      for (var subitemi = 0; subitemi < item.childNodes.length; subitemi++) {
        var subitem = item.childNodes[subitemi];
        if (subitem.nodeName.toLowerCase() == 'ul') {
          _explorerTreeSetState(subitem, collapse, excludedElements);
        }
      }
    }
  }
}

// Open the tree out so the list item with the link with the specified HREF is
// visible. Optionally scrolls so the item is visible. Optionally opens the
// found branch. Returns the LI that contains the specified HREF, or null if
// unsuccessful.
function explorerTreeOpenTo(id, href, scroll, expand) {
  var li = _explorerTreeSearch(document.getElementById(id), _explorerTreeNormalizeHref(href));
  if (li) {
    if (!window.IE7 || explorerTreeIE7Supported) { 
      if (explorerTreeAutoCollapse[id]) {
        _explorerTreeCollapseAllButElement(li);
        explorerTreeDocRecalc();
      }
      if (expand) {
        _explorerTreeOpen(li);
        explorerTreeDocRecalc();
      }
    }
    if (scroll) {
      // get height of window we're in
      var h;
      if (window.innerHeight) {
        // Netscape, Mozilla, Opera
        h = window.innerHeight;
      } else if (document.documentElement && document.documentElement.clientHeight) {
        // IE6 in 'standards' mode
        h = document.documentElement.clientHeight;
      } else if (document.body && document.body.clientHeight) {
        // other IEs
        h = document.body.clientHeight;
      } else {
        h = 0;
      }
      // scroll so the list item is centered on the window
      window.scroll(0, li.offsetTop - h / 2);
    }
  }
  return li;
}

// Search the list (and sub-lists) for the given href. Returns the LI object if
// found, otherwise returns null.
function _explorerTreeSearch(ul, href) {
  if (!ul.childNodes || ul.childNodes.length == 0) return null;
  // Iterate LIs
  for (var itemi = 0; itemi < ul.childNodes.length; itemi++) {
    var item = ul.childNodes[itemi];
    if (item.nodeName.toLowerCase() == 'li') {
      for (var subitemi=0; subitemi < item.childNodes.length; subitemi++) {
        var subitem = item.childNodes[subitemi];
        if (subitem.nodeName.toLowerCase() == 'a') {
          if (_explorerTreeNormalizeHref(subitem.getAttribute('href')) == href) {
            return item;
          }
        }
        if (subitem.nodeName.toLowerCase() == 'ul') {
          var found = _explorerTreeSearch(subitem, href);
          if (found) {
            _explorerTreeOpen(item);
            return found;
          }
        }
      }
    }
  }
  return null;
}

// When Opera performs HTMLElement.getAttribute('href'), it *doesn't* actually
// return the raw HREF like it's supposed to. It 'normalizes' it, adding in any
// missing protocol, host name/port, and converts relative HREFs (eg
// '../../index.html') into absolute HREFs (eg '/index.html'). It does exactly
// the same thing in CSS generated content for the attr(href) function. If all
// browsers did that it would make URL comparisons trivial. Unfortunately, other
// browsers don't, and they're probably doing the right thing too by returning
// the href as it appears in the HTML.
// What this function does is normalize HREFs so we can do a meaningful
// comparison in *all* browsers.
function _explorerTreeNormalizeHref(href) {
  var i, h = href, l = window.location;
  
  // immediately return explicit protocols
  if (href.substring(0, 7) == 'telnet:') return href;
  if (href.substring(0, 7) == 'mailto:') return href;
  if (href.substring(0, 7) == 'gopher:') return href;
  if (href.substring(0, 5) == 'http:'  ) return href;
  if (href.substring(0, 5) == 'news:'  ) return href;
  if (href.substring(0, 5) == 'rtsp:'  ) return href;
  
  // handle absolute references
  if (h.charAt(0) == '/') {
    return l.protocol + '//' + l.host + h;
  }
  
  // strip off the filename (if any) of the location to leave the folder we're in
  l = l.toString();
  i = l.lastIndexOf('/');
  if (i != -1) {
    l = l.substring(0, i + 1);
  }
  
  // handle any relative directory references, i.e. '../'
  while (h.substring(0, 3) == '../') {
    h = h.substring(3);
    i = l.lastIndexOf('/', l.length - 2);
    if (i != -1) {
      l = l.substring(0, i + 1);
    }
  }
  
  return l + h;
}

addEvent(window, 'load', explorerTreeRefreshAll, false);


