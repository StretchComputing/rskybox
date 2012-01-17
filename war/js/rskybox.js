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


  r.isValidEmailAddress = function (emailAddress) {
    return (/^[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+(?:\.[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+)*@(?:[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?$/).test(emailAddress);
  };

  return r;
}(rskybox || {}, jQuery));
