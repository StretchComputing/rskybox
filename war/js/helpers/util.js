var RSKYBOX = (function (r, $) {
  'use strict';


  r.dump = function (object) {
    try {
      r.log.local(JSON.stringify(object));
    } catch (e) {
      r.log.error(e, 'RSKYBOX.dump');
    }
  };


  // General status code handlers.
  // apiError: optional handler for API errors
  r.statusCodeHandlers = function (apiError) {
    var general = {
      401: function (jqXHR) {
        try {
          r.log.info('401 - unauthorized', 'RSKYBOX.statusCodeHandlers');
          r.flash.set('warning', 'Login required');
          r.logOut();
        } catch (e) {
          r.log.warn(e, 'RSKYBOX.statusCodeHandlers:general:401');
        }
      },
      404: function () {
        // TODO - display a 404 page
        r.log.warn('404 - not found', 'RSKYBOX.statusCodeHandlers');
      },
      500: function () {
        // TODO - display a 500 page
        r.log.warn('500 - server error', 'RSKYBOX.statusCodeHandlers');
      }
    };

    try {
      if (apiError) {
        $.extend(general, { 422: apiError });
      }
      return general;
    } catch (e) {
      r.log.error(e, 'RSKYBOX.statusCodeHandlers');
    }
  };


  // Handle logging in and logging out.
  r.logIn = function (token) {
    try {
      var dest = r.destination.get();
      r.log.info('entering', 'RSKYBOX.logIn');

      Cookie.set('token', token, 9000, '/');

      if (dest) {
        r.destination.remove();
        window.location = dest;
      } else {
        r.changePage('applications');
      }
    } catch (e) {
      r.log.error(e, 'RSKYBOX.logIn');
    }
  };

  r.logOut = function () {
    try {
      r.log.info('entering', 'RSKYBOX.logOut');

      Cookie.unset('token', '/');
      r.changePage('root', 'signup');
      r.session.reset();
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


  // Manage page redirections
  r.destination = {
    key: 'destination',

    get: function () {
      return r.store.getItem(this.key);
    },

    set: function (value) {
      r.store.setItem(this.key, value);
    },

    remove: function () {
      return r.store.removeItem(this.key);
    },
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
        r.store.setItem('flash', value);
      } catch (e) {
        r.log.error(e, 'flash.set');
      }
    };

    flash.check = function () {
      try {
        var value = r.store.getItem('flash');
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
        r.store.removeItem('flash');
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

        return window.dateFormat(date, 'ddd, mmm d yyyy, HH:MM:ss' + (showMilliseconds ? '.l' : ''));
      } catch (e) {
        r.log.error(e, 'format.longDate');
      }
    },
    compactDate: function (isoDate) {
      try {
        var date = new Date(isoDate);

        return window.dateFormat(date, 'yyyy/mm/dd HH:MM:ss.l');
      } catch (e) {
        r.log.error(e, 'format.timeOnly');
      }
    },
    timeOnly: function (isoDate) {
      try {
        var date = new Date(isoDate);

        return window.dateFormat(date, 'HH:MM:ss.l');
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
        RSKYBOX.log.warn('inappropriate operator', 'pageLoad');
      }
      return pageLoadCount;
    } catch (e) {
      RSKYBOX.log.error(e, 'pageLoad');
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
      RSKYBOX.log.error(e, 'ajaxSend');
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
      RSKYBOX.log.warn(e, 'ajaxComplete');
    }
  });
}(jQuery));
