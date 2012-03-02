var RSKYBOX = (function (r, $) {
  'use strict';


  r.dump = function (object) {
    console.log(JSON.stringify(object));
  };


  var rSkybox = {
    // Terry's dev app
    //appId: '',
    //authToken: 'Basic ',

    // Production app
    //appId: 'ahRzfnJza3lib3gtc3RyZXRjaGNvbXITCxILQXBwbGljYXRpb24Y0c4NDA',
    //authToken: 'Basic TG9naW46MnNwa2RlN2Y1dTdlNnU1Nzg2aXA1djl1ZjE=',
  };

  r.SkyboxLog = r.Log.extend({
    initialize: function () {
      _.bindAll(this, 'success', 'apiError');
      this.on('error', this.errorHandler, this);
    },

    logLevels: {
      exception: 5,
      error: 10,
      info: 25,
      debug: 50,
      local: 75,
      off: 99
    },

    exception: function (message, logName) {
      this.base('exception', message, logName);
    },

    error: function (message, logName) {
      this.base('error', message, logName);
    },

    info: function (message, logName) {
      this.base('info', message, logName);
    },

    debug: function (message, logName) {
      this.base('debug', message, logName);
    },

    local: function (message, logName) {
      this.base('local', message, logName);
    },

    base: function (level, message, logName) {
      var
        localLevel = this.logLevels.debug,
        serverLevel = this.logLevels.error;

      if (localLevel >= this.logLevels[level]) {
        console.log(level + (logName ? '(' + logName + ')' : '') + ': ' + message);
      }

      if ((serverLevel >= this.logLevels[level]) && this.get('appId') &&
          rSkybox.appId && rSkybox.authToken) {
        this.logToServer(level, message, logName);
      }
    },


    // Server functionality for the rest of the class below.
    logToServer: function (level, message, logName) {
      var attrs = {
        logName: logName,
        logLevel: level,
        message: message,
        userName: Cookie.get('token'),
      };

      this.save(attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
        headers: {
          Authorization: rSkybox.authToken,
        },
      });
    },

    success: function (model, response) {
      r.log.local('Skybox.log.error saved');
    },

    errorHandler: function (model, response) {
      r.log.local('SettingsView.error');
      if (response.responseText) {
        // This is an apiError.
        return;
      }
      // This is a validation error.
      r.flash.error(response);
    },

    apiError: function (jqXHR) {
      r.log.local('SettingsView.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.local('SettingsView: An unknown API error occurred: ' + code);
      }

      r.flash.error(this.apiCodes[code]);
    },

    apiCodes: {
      202: 'Invalid log level.',
      305: 'Application ID required.',
      315: 'Log name is required.',
      414: 'Invalid timestamp parameter.',
      415: 'Invalid duration parameter.',
      605: 'Application not found.',
    },
  });

  r.log = new r.SkyboxLog({});
  r.log.setAppUrl(rSkybox.appId);
  r.log.set('appId', rSkybox.appId);


  // General status code handlers.
  // apiError: optional handler for API errors
  r.statusCodeHandlers = function (apiError) {
    var general = {
      401: function (jqXHR) {
        r.log.debug('401 - unauthorized');
        r.unsetCookie();
        r.changePage('root', 'signup');
        // TODO - Add flash message to home page after 401 occurs.
      },
      404: function () {
        r.log.debug('404 - not found');
      },
      500: function () {
        r.log.debug('500 - server error');
      }
    };
    if (apiError) {
      $.extend(general, { 422: apiError });
    }
    return general;
  };


  r.setCookie = function (token) {
    Cookie.set('token', token, 9000, '\/');
  };

  r.unsetCookie = function () {
    Cookie.unset('token', '\/');
  };


  // Change to a new HTML page.
  r.changePage = function (page, area) {
    var
      base,
      newPage,
      pages = {
        root: '',
        login: '#login',
        applications: '',
        settings: '#settings',
      };

    switch (area) {
    case 'signup':
      base = '/';
      break;
    case 'admin':
      base = '/html5/admin';
      break;
    default:
      base = '/html5';
      break;
    }


    if (pages[page] === undefined) {
      r.log.error("RSKYBOX.changePage: page '" + page + "' not found.");
      return;
    }

    newPage = base + pages[page];
    r.log.debug("RSKYBOX.changePage: page '" + newPage + "'.");
    window.location = newPage;
  };


  r.getContentDiv = function () {
    return $.mobile.activePage.find(":jqmData(role='content')");
  };

  r.flash = (function () {
    var display, flash = {};

    // type: string indicating type of message; 'error', 'notice', etc.
    // message: message to display
    // el: the container to display the message within, or undefined to display in main content area
    display = function (type, message, el) {
      var flash, selector;

      selector = '.flash.' + type;
      el = el || r.getContentDiv();

      el.find(selector).remove();

      flash = $('<div>', {
        class: 'flash ' + type,
        text: message
      });

      $(el).prepend(flash);
    };

    flash.notice = function (message, el) {
      display('notice', message, el);
    };

    flash.error = function (message, el) {
      message = message || 'An unknown error occurred. Please reload the page to try again.';
      display('error', message, el);
    };

    return flash;
  }());


  // Add a property to an object, but only if it is defined and not blank.
  r.addProperty = function (object, property, value) {
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
    Object.keys(props).forEach(function (prop) {
      query += prop + '=' + props[prop] + '&';
    });
    query = query.slice(0, query.length - 1);

    return query;
  };


  // Pull the apiStatus value out of an HTTP error response.
  r.getApiStatus = function (responseText) {
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
}(RSKYBOX || {}, jQuery));


// This is here so we automatically get page loading messages when Ajax requests start and
// they are hidden when the Ajax requests are complete.
(function ($) {
  'use strict';
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

  $('html').ajaxSend(function (event, jqXHR, settings) {
    if (settings.headers && settings.headers.Authorization) {
      return;
    }
    RSKYBOX.log.debug('ajaxSend: ' + settings.url);
    showPageLoadingMessage();
  });

  $('html').ajaxComplete(function (event, jqXHR, settings) {
    if (settings.headers && settings.headers.Authorization) {
      return;
    }
    RSKYBOX.log.debug('ajaxComplete: ' + settings.url);
    hidePageLoadingMessage();
  });
}(jQuery));
