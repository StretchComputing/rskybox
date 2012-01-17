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


  // TODO - placeholder until we can do something consistent/logical with errors/warnings
  r.displayWarning = function(error) {
    console.log(error);
  };


  // Simple RegEx to ensure a valid phone number format.
  r.isValidPhoneNumber = function (phoneNumber) {
    return (/^\(?([0-9]{3})\)?[\-. ]?([0-9]{3})[\-. ]?([0-9]{4})$/).test(phoneNumber);
  };


  // Simple RegEx to ensure a valid email address format.
  r.isValidEmailAddress = function (emailAddress) {
    return (/^[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+(?:\.[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+)*@(?:[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?$/).test(emailAddress);
  };


  return r;
}(rskybox || {}, jQuery));
