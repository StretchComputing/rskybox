var RSKYBOX = (function (r, $) {
  'use strict';


  r.dump = function (object) {
    try {
      // TODO - log to localStorage
      console.log(JSON.stringify(object));
    } catch (e) {
      console.error(e, 'RSKYBOX.dump');
    }
  };


  var
    apiError = function (jqXHR) {
      try {
        var
          apiCodes = r.log.getApiCodes(),
          code = r.getApiStatus(jqXHR.responseText);

        // TODO - log to localStorage
        console.info(code, 'RSKYBOX.log.apiError');

        if (!apiCodes[code]) {
          // TODO - log to localStorage
          console.info('Undefined apiStatus: ' + code, 'SkyboxLog.apiError');
        }
        r.flash.warning(apiCodes[code]);
      } catch (e) {
        // TODO - log to localStorage
        console.error(e, 'RSKYBOX.log.apiError');
      }
    },

    error = function (model, response) {
      try {
        if (response.responseText) { return; }  // This is an apiError.

        // TODO - log to localStorage
        console.warn(response, 'RSKYBOX.log.error');
        r.flash.warning(response);              // This is a validation error.
      } catch (e) {
        // TODO - log to localStorage
        console.error(e, 'RSKYBOX.log.error');
      }
    },

    getSummary = function () {
    },

    getUserName = function () {
      try {
        var
          name = '',
          user = r.session && r.session.getEntity(r.session.keys.currentUser);

        if (user.firstName) { name += user.firstName + ' '; }
        if (user.lastName) { name += user.lastName; }
        if (name) { name += ', '; }
        if (user.emailAddress) { name += user.emailAddress; }
        if (user.phoneNumber) { name += ', ' + user.phoneNumber; }

        if (!name) { name = Cookie.get('token'); }

        return name;
      } catch (e) {
        r.log.error(e, 'RSKYBOX.log.getUserName');
      }
    },

    logLevels = r.log.getLogLevels(),

    success = function (model, response) {
      console.info('entering', 'SkyboxLog.success');
    };

  // appId will be null if user is not logged in.
  // This will produce an error log in the console.
  $(function () {
    try {
      var settings = {};

      settings.appId = Cookie.get('appId');
      settings.authHeader = 'Basic ' + Cookie.get('authHeader');
      settings.userName = getUserName();
      settings.summary = getSummary();
      settings.instanceUrl = location.hash;
      settings.success = success;
      settings.error = error;
      settings.statusCode = r.statusCodeHandlers(apiError);

      r.log.initialize(settings);
    } catch (e) {
      // TODO - log to localStorage
      console.error(e, 'RSKYBOX.log.initialize');
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));



var RSKYBOX = (function (r, $) {
  'use strict';


  var storage = {
      clear: function () {
        localStorage.clear();
      },

      setItem: function (item, value) {
        r.log.info(item, 'storage.setItem');

        localStorage.setItem(item, JSON.stringify(value));
      },

      getItem: function (item) {
        try {
          var results;
          r.log.info(item, 'storage.getItem');

          results = JSON.parse(localStorage.getItem(item));
          if (!results || results === '' || results === 'fetching') {
            return false;
          }
          return results;
        } catch (e) {
          r.log.error(e, 'storage.getItem');
        }
      },
    };


  // General status code handlers.
  // apiError: optional handler for API errors
  r.statusCodeHandlers = function (apiError) {
    var general = {
      401: function (jqXHR) {
        try {
          // TODO - log to localStorage
          console.info('401 - unauthorized', 'RSKYBOX.statusCodeHandlers');
          // TODO - Add flash message to home page after 401 occurs
          r.flash.set('warning', 'Login required');
          r.logOut();
        } catch (e) {
          // TODO - log to localStorage
          console.warn(e, 'RSKYBOX.statusCodeHandlers:general:401');
        }
      },
      404: function () {
        // TODO - display a 404 page
        // TODO - log to localStorage
        console.warn('404 - not found', 'RSKYBOX.statusCodeHandlers');
      },
      500: function () {
        // TODO - display a 500 page
        // TODO - log to localStorage
        console.warn('500 - server error', 'RSKYBOX.statusCodeHandlers');
      }
    };

    try {
      if (apiError) {
        $.extend(general, { 422: apiError });
      }
      return general;
    } catch (e) {
      // TODO - log to localStorage
      console.error(e, 'RSKYBOX.statusCodeHandlers');
    }
  };


  // Handle logging in and logging out.
  r.logIn = function (token) {
    try {
      r.log.info('entering', 'RSKYBOX.logIn');

      Cookie.set('token', token, 9000, '/');
      r.changePage('applications');
    } catch (e) {
      r.log.error(e, 'RSKYBOX.logIn');
    }
  };

  r.logOut = function () {
    try {
      r.log.info('entering', 'RSKYBOX.logOut');

      Cookie.unset('token', '/');
      r.changePage('root', 'signup');
      sessionStorage.clear();
    } catch (e) {
      r.log.error(e, 'RSKYBOX.logOut');
    }
  };

  r.isLoggedIn = function () {
    try {
      r.log.info('entering', 'RSKYBOX.isLoggedIn');

      return !!Cookie.get('token');
    } catch (e) {
      r.log.error(e, 'RSKYBOX.isLoggedIn');
    }
  };


  // Change to a new HTML page.
  r.changePage = function (page, area, params) {
    try {
      var newPage, pages, query;
      r.log.info('entering', 'RSKYBOX.changePage');

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
      r.log.error(e, 'RSKYBOX.changePage');
    }
  };


  r.getHeaderDiv = function () {
    try {
      return $.mobile.activePage.find(":jqmData(role='header')");
    } catch (e) {
      r.log.error(e, 'RSKYBOX.getHeaderDiv');
    }
  };

  r.getContentDiv = function () {
    try {
      return $.mobile.activePage.find(":jqmData(role='content')");
    } catch (e) {
      r.log.error(e, 'RSKYBOX.getContentDiv');
    }
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
        r.log.info(message, 'flash.display');
        flash.clear();
      } catch (e) {
        r.log.error(e, 'flash.display');
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
        var value = { type: type, message: message, };
        r.log.info('entering', 'flash.set');

        if (duration) { value.duration = duration; }
        storage.setItem('flash', value);
      } catch (e) {
        r.log.error(e, 'flash.set');
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
          r.log.warn('unknown flash type', 'flash.check');
          break;
        }
      } catch (e) {
        r.log.error(e, 'flash.check');
      }
    };

    flash.clear = function () {
      try {
        localStorage.removeItem('flash');
      } catch (e) {
        r.log.error(e, 'flash.clear');
      }
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
      r.log.error(e, 'RSKYBOX.buildQueryString');
    }
  };


  // Pull the apiStatus value out of an HTTP error response.
  r.getApiStatus = function (responseText) {
    try {
      return JSON.parse(responseText).apiStatus;
    } catch (e) {
      r.log.error(e, 'RSKYBOX.getApiStatus');
    }
  };


  // Simple RegEx to ensure a valid phone number format.
  r.isValidPhoneNumber = function (phoneNumber) {
    try {
      return (/^\(?([0-9]{3})\)?[\-. ]?([0-9]{3})[\-. ]?([0-9]{4})$/).test(phoneNumber);
    } catch (e) {
      r.log.error(e, 'RSKYBOX.isValidPhoneNumber');
    }
  };


  // Simple RegEx to ensure a valid email address format.
  r.isValidEmailAddress = function (emailAddress) {
    try {
      return (/^[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+(?:\.[a-z0-9!#$%&'*+\/=?\^_`{|}~\-]+)*@(?:[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9\-]*[a-z0-9])?$/).test(emailAddress);
    } catch (e) {
      r.log.error(e, 'RSKYBOX.isValidEmailAddress');
    }
  };


  r.format = {
    longDate: function (isoDate, showMilliseconds) {
      try {
        var date = new Date(isoDate);

        return window.dateFormat(isoDate, 'ddd, mmm d yyyy, HH:MM:ss' + (showMilliseconds ? '.l' : ''));
      } catch (e) {
        r.log.error(e, 'format.longDate');
      }
    },
    timeOnly: function (isoDate) {
      try {
        var date = new Date(isoDate);

        return window.dateFormat(isoDate, 'HH:MM:ss.l');
      } catch (e) {
        r.log.error(e, 'format.timeOnly');
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
      // TODO - log to localStorage
        console.warn('inappropriate operator', 'pageLoad');
      }
      return pageLoadCount;
    } catch (e) {
      // TODO - log to localStorage
      console.error(e, 'pageLoad');
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
      console.error(e, 'ajaxSend');
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
      console.warn(e, 'ajaxComplete');
    }
  });
}(jQuery));
