'use strict';

// The main namespace for our application
var rskybox = (function(r, $) {

  // Returns the value of a named parameter from a given JQM URL.
  r.getParameterByName = function (url, name) {
    var match = new RegExp('[?&]' + name + '=([^&]*)').exec(url);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
  };


  // Returns the currently valid REST path prefix.
  r.getRestPrefix = function () {
    var appId, restUrl;

    restUrl = '\/rest\/v1';
    if (window.location.search) {
      appId = r.getParameterByName(window.location.search, 'id');
      if (appId) {
        restUrl += '\/applications\/' + appId;
      }
    }
    return restUrl;
  };



  // The sandbox is what the modules call to get work done.
  // The sandbox can make calls to the core.
  r.sandbox = {
  };


  // The core functionality of our app.  Only the sandbox should make calls into it.
  r.core = {
  };


  return r;
}(rskybox || {}, jQuery));
