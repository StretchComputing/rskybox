//
// rSkybox JavaScript Agent Configuration, v0.1 Beta, 2012/05/02 14:00 EDT
//
// Copyright © 2012 by Stretch Computing, Inc. All rights reserved.
//
// Any redistribution or reproduction of part or all of the contents in any
// form is prohibited without prior approval. You may not, except with our
// express written permission, distribute or commercially exploit the content.
//

var RSKYBOX = (function (r) {
  'use strict';


  var receiveMessage = function (event) {
    var data = event.data.data;

    if (data.appConfig) {
      r.config.appConfig = data.appConfig;
    }

    switch (event.data.level) {
    case 'error':
      r.log.error(data.message, data.name);
      break;
    case 'warn':
      r.log.warn(data.message, data.name);
      break;
    case 'info':
      r.log.info(data.message, data.name);
      break;
    case 'debug':
      r.log.debug(data.message, data.name);
      break;
    case 'local':
      r.log.local(data.message, data.name);
      break;
    default:
      window.console.log('unknown level received ' + event.data.level);
      break;
    }
  };

  window.addEventListener('message', receiveMessage, false);


  // Make sure we have an object to work with.
  r.config = {
    appWindow: window.parent,

    // A function that returns the object to use for logging locally.
    // Must support the following methods: error(), warn(), info(), debug(), log().
    // Return nothing or undefined to turn off local logging.
    getConsole: function () {
      return window.console;
    },


    // Your application's name.
    getApplicationName: function () {
      try {
        return this.appConfig.applicationName || 'not set';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getApplicationName');
      }
    },


    // Your application's version.
    getApplicationVersion: function () {
      try {
        return this.appConfig.applicationVersion || 'not set';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getApplicationVersion');
      }
    },


    // Your rSkybox application ID.
    getApplicationId: function () {
      try {
        return this.appConfig.applicationId;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getApplicationId');
      }
    },


    // Your rSkybox authentication token.
    getAuthHeader: function () {
      try {
        return this.appConfig.authHeader;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getAuthHeader');
      }
    },


    // Track information about who experienced the issue here.
    getUserId: function () {
      try {
        return this.appConfig.userId || 'not set';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getUserId');
      }
    },

    getUserName: function () {
      try {
        return this.appConfig.userId || 'not set';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getUserName');
      }
    },


    // Set the url of the current page, or use the field for some other type of
    // information you want to track.
    getInstanceUrl: function () {
      try {
        return this.appConfig.instanceUrl;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getInstanceUrl');
      }
    },


    // A good place to put information about the user's environment.
    getSummary: function () {
      try {
        return this.appConfig.summary;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getSummary');
      }
    },


    getAppActions: function () {
      try {
        return this.appConfig.appActions;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.config.getAppActions');
      }
    },


    log: {
      // The highest log level to use for logging to the server.
      // Levels in increasing value are: error, warn, info, debug, local, off.
      getServerLevel: function () {
        try {
          return r.log.getLevels().warn;
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
          window.console.info('entering', 'RSKYBOX.config.log.successHandler');
        } catch (e) {
          window.console.error(e, 'RSKYBOX.config.log.getSuccessHandler');
        }
      },


      // A callback function to respond to errors returned by the REST/Ajax call.
      errorHandler: function (jqXHR, textStatus, errorThrown) {
        try {
          if (jqXHR.responseText) { return; }  // This is an apiError.
          window.console.warn(textStatus, 'RSKYBOX.config.log.errorHandler');
        } catch (e) {
          window.console.error(e, 'RSKYBOX.config.log.errorHandler');
        }
      },


      // An object compatible with jQuery's Ajax statusCode option.
      // This is an object of key/value pairs where the key is the status code to
      // respond to, and the value is the callback function that responds.
      // rSkybox API errors are returned in HTTP code 422.
      statusCodeHandlers: {
        422: function (jqXHR) {
          try {
            var
              apiCodes = r.log.getApiCodes(),
              code = JSON.parse(jqXHR.responseText).apiStatus;

            window.console.info(code, 'RSKYBOX.config.log.apiErrorHandler');

            if (!apiCodes[code]) {
              window.console.error('Undefined apiStatus: ' + code, 'RSKYBOX.config.log.apiErrorHandler');
            }
          } catch (e) {
            window.console.error(e, 'RSKYBOX.config.log.apiErrorHandler');
          }
        }
      }
    }
  };

  return r;
}(RSKYBOX || {}));
