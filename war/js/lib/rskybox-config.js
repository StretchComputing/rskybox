var RSKYBOX = (function (r, $) {
  'use strict';


  // Make sure we have an object to work with.
  $.extend(r.log, {
    // A function that returns the object to use for logging locally.
    // Must support the following methods: error(), warn(), info(), debug(), log().
    // Return nothing or undefined to turn off local logging.
    getConsole: function () {
      return window.console;
    },


    // Your rSkybox application ID.
    getApplicationId: function () {
      try {
        return Cookie.get('appId');
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getApplicationId');
      }
    },


    // Your rSkybox authentication token.
    getAuthHeader: function () {
      try {
        return 'Basic ' + Cookie.get('authHeader');
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getAuthHeader');
      }
    },


    // Track information about who experience the issue here.
    getUserName: function () {
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
        window.console.error(e, 'RSKYBOX.log.getUserName');
      }
    },


    // Set the url of the current page, or use the field for some other type of
    // information you want to track.
    getInstanceUrl: function () {
      try {
        return location.hash;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getInstanceUrl');
      }
    },


    // A good place to put information about the user's environment.
    getSummary: function () {
      try {
        return 'temp summary';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getSummary');
      }
    },


    // The highest log level to use for logging to the server.
    // Levels in increasing value are: error, warn, info, debug, local, off.
    getServerLevel: function () {
      try {
        return r.log.getLevels().error;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getServerLevel');
      }
    },


    // The highest log level to use for logging to the local console.
    // Levels in increasing value are: error, warn, info, debug, local, off.
    getLocalLevel: function () {
      try {
        return r.log.getLevels().local;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getLocalLevel');
      }
    },


    // A callback function to respond to success returned by the REST/Ajax call.
    successHandler: function (data, status, jqXHR) {
      try {
        r.log.info('entering', 'RSKYBOX.log.successHandler');
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getSuccessHandler');
      }
    },


    // A callback function to respond to errors returned by the REST/Ajax call.
    errorHandler: function (jqXHR, textStatus, errorThrown) {
      try {
        if (jqXHR.responseText) { return; }  // This is an apiError.
        r.log.warn(textStatus, 'RSKYBOX.log.errorHandler');
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.errorHandler');
      }
    },


    // A callback function that will respond to various HTTP status codes.
    // API errors are returned HTTP code 422.
    statusCodeHandlers: r.statusCodeHandlers(function (jqXHR) {
      try {
        var
          apiCodes = r.log.getApiCodes(),
          code = r.getApiStatus(jqXHR.responseText);

        r.log.info(code, 'RSKYBOX.log.apiErrorHandler');

        if (!apiCodes[code]) {
          r.log.info('Undefined apiStatus: ' + code, 'RSKYBOX.log.apiErrorHandler');
        }
        r.flash.warning(apiCodes[code]);
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.apiErrorHandler');
      }
    }),
  });


  return r;
}(RSKYBOX || {}, jQuery));
