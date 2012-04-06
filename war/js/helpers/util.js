var RSKYBOX = (function (r, $) {
  'use strict';


  // **** These must be defined here so they can be used further down in this function. ****
  //
  // General status code handlers.
  // apiError: optional handler for API errors
  r.statusCodeHandlers = function (apiError) {
    var general = {
      401: function (jqXHR) {
        try {
          r.log.info('401 - unauthorized', 'statusCodeHandlers');
          // TODO - Add flash message to home page after 401 occurs
          r.flash.set('warning', 'Login required');
          r.logOut();
        } catch (e) {
          // TODO - log to localStorage
          console.log(e.stack, 'statusCodeHandlers.general.401:util.js');
        }
      },
      404: function () {
        r.log.error('404 - not found', 'statusCodeHandlers');
      },
      500: function () {
        r.log.error('500 - server error', 'statusCodeHandlers');
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

    exception: function (e, name) {
      // TODO - change to crashDetect call.
      // TODO - include environment information in summary
      // TODO - include error name in summary
      this.base('exception', e.stack, name);
    },

    error: function (message, name) {
      this.base('error', message, name);
    },

    info: function (message, name) {
      this.base('info', message, name);
    },

    debug: function (message, name) {
      this.base('debug', message, name);
    },

    local: function (message, name) {
      this.base('local', message, name);
    },

    base: function (level, message, name) {
      try {
        var
          localLevel = this.logLevels.local,
          output = '',
          serverLevel = this.logLevels.error;

        if (localLevel >= this.logLevels[level]) {
          output += this.logLevels[level] <= this.logLevels.error ? '***** ' : '';
          output += level.toUpperCase() + ' ';
          output += message;
          output += name ? ' \t(' + name + ')' : '';
          console.log(output);
        }

        if (this.get('appId') && (serverLevel >= this.logLevels[level])) {
          this.logToServer(level, message, name);
        }
      } catch (e) {
        // TODO - log to localStorage
        console.log(e.stack, 'rSkyboxLog.base:util.js');
      }
    },


    // Server functionality for the rest of the class below.
    logToServer: function (level, message, name) {
      try {
        var
          attrs = {
            name: name || message,
            logLevel: level,
            message: message,
            userName: Cookie.get('token'),
            instanceUrl: location.hash,
          },
          getUserName,
          user;

        user = r.session && r.session.getEntity(r.session.keys.currentUser);
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
        });
      } catch (e) {
        // TODO - log to localStorage
        console.log(e.stack, 'rSkyboxLog.logToServer:util.js');
      }
    },

    success: function (model, response) {
      r.log.local('entering', 'SkyboxLog.success');
    },

    errorHandler: function (model, response) {
      if (response.responseText) { return; }  // This is an apiError.
      r.log.local(response, 'SkyboxLog.error');
      r.flash.warning(response);              // This is a validation error.
    },

    apiError: function (jqXHR) {
      var code = r.getApiStatus(jqXHR.responseText);
      r.log.local(code, 'SkyboxLog.apiError');

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


  try {
    r.log = new r.SkyboxLog({});
    r.log.setAppUrl(Cookie.get('appId'));
    // appId will be null if user is not logged in.
    // This will produce an error log in the console.
    r.log.set('appId', Cookie.get('appId'));
  } catch (e) {
    // TODO - log to localStorage
    console.log(e.stack, 'logsetup:util.js');
  }


  return r;
}(RSKYBOX || {}, jQuery));



var RSKYBOX = (function (r, $) {
  'use strict';


  var storage = {
      clear: function () {
        localStorage.clear();
      },

      setItem: function (item, value) {
        r.log.info(item, 'storage.setItem.entering');
        localStorage.setItem(item, JSON.stringify(value));
      },

      getItem: function (item) {
        var results;

        r.log.info(item, 'storage.getItem.entering');

        results = JSON.parse(localStorage.getItem(item));
        if (!results || results === '' || results === 'fetching') {
          return false;
        }
        return results;
      },
    };


  // Handle logging in and logging out.
  r.logIn = function (token) {
    Cookie.set('token', token, 9000, '/');
    r.changePage('applications');
  };

  r.logOut = function () {
    Cookie.unset('token', '/');
    r.changePage('root', 'signup');
    sessionStorage.clear();
  };

  r.isLoggedIn = function () {
    return !!Cookie.get('token');
  };


  // Change to a new HTML page.
  r.changePage = function (page, area, params) {
    try {
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
    } catch (e) {
      r.log.exception(e, 'changePage:util.js');
    }
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
      try {
        var element;

        $('.flash').remove();

        element = $('<div>', {
          class: 'flash ' + type,
          text: message
        }).hide();

        $.mobile.activePage.prepend(element);
        element.fadeIn().delay(duration * 1000).fadeOut(600);
        r.log.debug(message, 'flash.display');
        flash.clear();
      } catch (e) {
        r.log.exception(e, 'flash.display:util.js');
      }
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

    flash.set = function (type, message, duration) {
      try {
        r.log.info('entering', 'flash.set');
        var value = { type: type, message: message, };

        if (duration) { value.duration = duration; }
        storage.setItem('flash', value);
      } catch (e) {
        r.log.exception(e, 'flash.set:util.js');
      }
    };

    flash.check = function () {
      try {
        var value = storage.getItem('flash');
        r.log.info('entering', 'flash.check');

        if (!value) { return; }

        switch (value.type) {
        case 'success':
          flash.success(value.message, value.duration);
          break;
        case 'info':
          flash.info(value.message, value.duration);
          break;
        case 'warning':
          flash.warning(value.message, value.duration);
          break;
        case 'error':
          flash.error(value.message, value.duration);
          break;
        default:
          r.log.error('unknown flash type', 'flash.check');
          break;
        }
      } catch (e) {
        r.log.exception(e, 'flash.check:util.js');
      }
    };

    flash.clear = function () {
      localStorage.removeItem('flash');
    };

    return flash;
  }());


  // Build a query string from an object.
  //
  // props: the object containing the name/value pairs for the query string
  r.buildQueryString = function (props) {
    try {
      var prop, query;

      query = "?";
      Object.keys(props).forEach(function (prop) {
        query += prop + '=' + props[prop] + '&';
      });
      query = query.slice(0, query.length - 1);

      return query;
    } catch (e) {
      r.log.exception(e, 'buildQueryString:util.js');
    }
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
      try {
        var date = new Date(isoDate);

        return window.dateFormat(isoDate, 'ddd, mmm d yyyy, HH:MM:ss' + (showMilliseconds ? '.l' : ''));
      } catch (e) {
        r.log.exception(e, 'format.longDate:util.js');
      }
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
    try {
      switch (operator) {
      case 'decrement':
        pageLoadCount -= pageLoadCount === 0 ? 0 : 1;
        break;
      case 'increment':
        pageLoadCount += 1;
        break;
      default:
        console.log('pageLoadingCount called with inappropriate operator.');
      }
      return pageLoadCount;
    } catch (e) {
      RSKYBOX.log.exception(e, 'pageLoad:util.js');
    }
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
    try {
      if (settings.headers && settings.headers.Authorization) {
        return;
      }
      RSKYBOX.log.local(settings.url, 'ajaxSend');
      showPageLoadingMessage();
    } catch (e) {
      // TODO - log to localStorage
      console.log(e.stack, 'ajaxSend:util.js');
    }
  });

  $('html').ajaxComplete(function (event, jqXHR, settings) {
    try {
      if (settings.headers && settings.headers.Authorization) {
        return;
      }
      RSKYBOX.log.local(settings.url, 'ajaxComplete');
      hidePageLoadingMessage();
    } catch (e) {
      // TODO - log to localStorage
      console.log(e.stack, 'ajaxComplete:util.js');
    }
  });
}(jQuery));
