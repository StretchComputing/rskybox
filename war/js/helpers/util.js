var RSKYBOX = (function (r, $) {
  'use strict';


  var rSkybox = {
    appId: Cookie.get('appId'),
  };

  // **** These must be defined here so they can be used further down in this function. ****
  //
  // General status code handlers.
  // apiError: optional handler for API errors
  r.statusCodeHandlers = function (apiError) {
    var general = {
      401: function (jqXHR) {
        r.log.info('401 - unauthorized');
        r.logOut();
        // TODO - Add flash message to home page after 401 occurs
      },
      404: function () {
        r.log.error('404 - not found');
      },
      500: function () {
        r.log.error('500 - server error');
      }
    };
    if (apiError) {
      $.extend(general, { 422: apiError });
    }
    return general;
  };


  r.dump = function (object) {
    r.log.debug(JSON.stringify(object));
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
        localLevel = this.logLevels.local,
        output = '',
        serverLevel = this.logLevels.error;

      if (localLevel >= this.logLevels[level]) {
        output += this.logLevels[level] <= this.logLevels.error ? '***** ' : '';
        output += level.toUpperCase() + ' ';
        output += message;
        output += logName ? ' \t(' + logName + ')' : '';
        console.log(output);
      }

      if (this.get('appId') && (serverLevel >= this.logLevels[level])) {
        this.logToServer(level, message, logName);
      }
    },


    // Server functionality for the rest of the class below.
    logToServer: function (level, message, logName) {
      var
        attrs = {
          logName: logName || message,
          logLevel: level,
          message: message,
          userName: Cookie.get('token'),
        },
        getUserName,
        user;

      user = r.session.getEntity(r.session.keys.currentUser);
      if (user) {
        getUserName = function () {
          var name = '';

          if (user.firstName) { name += user.firstName + ' '; }
          if (user.lastName) { name += user.lastName; }
          if (name) { name += ', '; }
          if (user.emailAddress) { name += user.emailAddress; }
          if (user.phoneNumber) { name += ', ' + user.phoneNumber; }

          if (!name) { name = Cookie.get('token'); }

          return name;
        };
        attrs.userName = getUserName();
      }

      this.save(attrs, {
        success: this.success,
        statusCode: r.statusCodeHandlers(this.apiError),
        headers: {
          Authorization: rSkybox.authToken,
        },
      });
    },

    success: function (model, response) {
      r.log.local('SkyboxLog.success');
    },

    errorHandler: function (model, response) {
      r.log.local('SkyboxLog.error');
      if (response.responseText) { return; }  // This is an apiError.
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      r.log.local('SkyboxLog.apiError');
      var code = r.getApiStatus(jqXHR.responseText);

      if (!this.apiCodes[code]) {
        r.log.local('Undefined apiStatus: ' + code, 'SkyboxLog.apiError');
      }
      r.flash.warning(this.apiCodes[code]);
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
  // appId will be null if user is not logged in.
  // This will produce an error log in the console.
  r.log.set('appId', rSkybox.appId);


  // Handle logging in and logging out.
  r.logIn = function (token) {
    Cookie.set('token', token, 9000, '/');
    r.changePage('applications');
  };

  r.logOut = function () {
    Cookie.unset('token', '/');
    r.changePage('root', 'signup');
  };

  r.isLoggedIn = function () {
    return !!Cookie.get('/');
  };


  // Change to a new HTML page.
  r.changePage = function (page, area, params) {
    var newPage, pages, query;

    if (!page) { page = ''; }
    if (!area) { area = 'app'; }

    pages = {
      signup: {
        base: '/',
        root: '',
        login: '#login',
        confirm: '#confirm',
      },
      app: {
        base: '/html5',
        applications: '',
        settings: '#settings',
      },
      admin: {
        base: '/html5/admin',
      },
    };

    newPage = pages[area].base + pages[area][page];
    if (params) { newPage += r.buildQueryString(params); }

    r.log.info(newPage, 'RSKYBOX.changePage');
    window.location = newPage;
  };


  r.getHeaderDiv = function () {
    return $.mobile.activePage.find(":jqmData(role='header')");
  };

  r.getContentDiv = function () {
    return $.mobile.activePage.find(":jqmData(role='content')");
  };

  r.flash = (function () {
    var display, flash = {};

    // type: string indicating type of message; 'error', 'notice', etc.
    // message: message to display
    // duration: time in seconds to leave flash on screen
    display = function (type, message, duration) {
      var flash;

      $('.flash').remove();

      flash = $('<div>', {
        class: 'flash ' + type,
        text: message
      }).hide();

      $.mobile.activePage.prepend(flash);
      flash.fadeIn().delay(duration * 1000).fadeOut(600);
    };

    flash.success = function (message, duration) {
      display('success', message, duration || 3);
    };

    flash.info = function (message, duration) {
      display('info', message, duration || 5);
    };

    flash.warning = function (message, duration) {
      display('warning', message, duration || 7);
    };

    flash.error = function (message, duration) {
      message = message || 'An unknown error occurred. Please reload the page to try again.';
      display('error', message, duration || 10);
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


  r.format = {
    longDate: function (isoDate, showMilliseconds) {
      var date = new Date(isoDate);

      return window.dateFormat(isoDate, 'ddd, mmm d yyyy, hh:MM:ss.l');
      //return date.toDateString() + ', ' + date.getHourstoTimeString() + (showMilliseconds ? '.' + date.getMilliseconds() : '');
    },
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
    RSKYBOX.log.local(settings.url, 'ajaxSend');
    showPageLoadingMessage();
  });

  $('html').ajaxComplete(function (event, jqXHR, settings) {
    if (settings.headers && settings.headers.Authorization) {
      return;
    }
    RSKYBOX.log.local(settings.url, 'ajaxComplete');
    hidePageLoadingMessage();
  });
}(jQuery));
