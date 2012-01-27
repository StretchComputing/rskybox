'use strict';


// The main namespace for our application
var rskybox = (function(r, $) {


  r.log = {
    logLevels: {
      error: 1,
      info: 5,
      debug: 10
    },

    logLevel: function() {
      return this.logLevels.debug;
    },

    error: function(message) {
      if (this.logLevel() < this.logLevels.error) { return; }
      this.base('Error: ' + message);
    },

    info: function(message) {
      if (this.logLevel() < this.logLevels.info) { return; }
      this.base('Info: ' + message);
    },

    debug: function(message) {
      if (this.logLevel() < this.logLevels.debug) { return; }
      this.base('Debug: ' + message);
    },

    base: function(message) {
      console.log(message);
    }
  };


  // message: The error message to display.
  // el: If not specified, we'll use the active page's content area.
  r.flashError = function(message, el) {
    var flash, selector;

    el = el || $.mobile.activePage.find(":jqmData(role='content')");

    selector = '.flash.error';
    el.find(selector).remove();

    message = message || 'An unknown error occurred. Please reload the page to try again.';
    flash = $('<div>', {
      class: 'flash error',
      text: message
    });
    $(el).prepend(flash);
  };


  // Add a property to an object, but only if it is defined and not blank.
  r.addProperty = function(object, property, value) {
    if (object && property && value) {
      object[property] = value;
    }
  };


  // Returns the value of a named parameter from a given JQM URL.
  r.getParameterByName = function (url, name) {
    var match = new RegExp('[?&]' + name + '=([^&]*)').exec(url);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
  };


  // Build a query string from an object.
  //
  // props: the object containing the name/value pairs for the query string
  r.buildQueryString = function (props) {
    var prop, query;

    query = "?";
    for (prop in props) {
      query += prop + '=' + props[prop] + '&';
    }
    query = query.slice(0, query.length - 1);

    return query;
  };


  // Pull the apiStatus value out of an HTTP error response.
  r.getApiStatus = function(responseText) {
    return JSON.parse(responseText).apiStatus;
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
