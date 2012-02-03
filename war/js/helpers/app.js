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


  r.setCookie = function(token) {
    Cookie.set('token', token, 9000, '\/');
  },


  r.unsetCookie = function(token) {
    Cookie.unset('token', '/');
  },


  r.dump = function(object) {
    console.log(JSON.stringify(object));
  };


  // Change to a new HTML page.
  r.changePage = function(page, area) {
    var
      base,
      newPage,
      pages = {
        root: '\/',
        applications: '',
        settings: '#settings'
      };

    switch (area) {
      case 'signup':
        base = '';
        break;
      case 'admin':
        base = '\/html5\/admin';
        break;
      default:
        base = '\/html5';
        break;
    }


    if (pages[page] === undefined) {
      r.log.error("rskybox.changePage: page '" + page + "' not found.");
      return;
    }

    newPage = base + pages[page];
    r.log.debug("rskybox.changePage: page '" + newPage + "'.");
    window.location = newPage;
  };


  r.getContentDiv = function() {
    return $.mobile.activePage.find(":jqmData(role='content')");
  };

  // message: The error message to display.
  // el: If not specified, we'll use the active page's content area.
  r.flashError = function(message, el) {
    var flash, selector;

    el = el || r.getContentDiv();

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


// This is here so we automatically get page loading messages when Ajax requests start and
// they are hidden when the Ajax requests are complete.
(function() {
  var hidePageLoadingMessage, pageLoad, pageLoadCount, showPageLoadingMessage;

  pageLoadCount = 0;
  pageLoad = function (operator) {
    switch (operator) {
    case 'decrement':
      pageLoadCount -= pageLoadCount === 0 ? 0 : 1;
      break;
    case 'increment':
      pageLoadCount += 1;
      break;
    default:
      window.console.log('pageLoadingCount called with inappropriate operator.');
    }
    return pageLoadCount;
  };


  // Manage showing/hiding the page loading message based on the number of times it's been called.
  hidePageLoadingMessage = function () {
    if (pageLoad('decrement') <= 0) {
      $.mobile.hidePageLoadingMsg();
    }
  };

  showPageLoadingMessage = function () {
    pageLoad('increment');
    $.mobile.showPageLoadingMsg();
  };

  $('html').ajaxSend(function(event, jqXHR, settings) {
    rskybox.log.debug('ajaxSend: ' + settings.url);
    showPageLoadingMessage();
  });
  $('html').ajaxComplete(function(event, jqXHR, settings) {
    rskybox.log.debug('ajaxComplete: ' + settings.url);
    hidePageLoadingMessage();
  });
})();
