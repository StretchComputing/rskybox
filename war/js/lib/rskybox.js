var RSKYBOX = (function (r, $) {
  'use strict';


  var
    logLevels = {
      error: 5,
      warn: 10,
      info: 25,
      debug: 50,
      local: 75,
      off: 99
    },

    apiCodes = {
      202: 'Invalid log level.',
      305: 'Application ID required.',
      315: 'Log name is required.',
      414: 'Invalid timestamp parameter.',
      415: 'Invalid duration parameter.',
      605: 'Application not found.',
    },


    // The URL for the REST call to create an rSkybox log.
    getUrl = function () {
      try {
        //return 'https://rskybox-stretchcom.appspot.com/rest/v1/applications/' + r.log.getApplicationId() + '/clientLogs';
        return '/rest/v1/applications/' + r.log.getApplicationId() + '/clientLogs';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getUrl');
      }
    },


    // Log information at the console object provided.
    local = function (console, level, message, name) {
      try {
        var output;

        // Not defining the console turns off console logging.
        // Set the console object in the r.log.getConsole() method.
        if (!console) { return; }

        // Error level calls usually have an error object in the message parameter.
        // If message is not a string, assume it's an error object. (If it's not
        // stack should be undefined and shouldn't cause a problem.)
        if (typeof message === 'string') {
          output = message + (name ? ' \t(' + name + ')' : '');
        } else {
          output = (name ? name + ' \t' : '') + message.stack;
        }

        switch (level) {
        case 'error':
          console.error(output);
          break;
        case 'warn':
          console.warn(output);
          break;
        case 'info':
          console.info(output);
          break;
        case 'debug':
          console.debug('DEBUG ' + output);
          break;
        case 'local':
          console.log('LOCAL ' + output);
          break;
        default:
          console.log(output);
          break;
        }
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.local');
      }
    },


    // Log to localStorage to queue up logs when logging to the server is not available.
    cache = function (level, message, name) {
      // TODO - need functionality to log to localStorage
      local(r.log.getConsole(), level, message, name);
    },


    // Make sure we have valid attributes for logging to the server.
    isValid = function (attrs) {
      try {
        var console = r.log.getConsole();

        if (!attrs) {
          local(console, 'error', 'attrs not defined', 'RSKYBOX.log.isValid');
          return false;
        }
        if (!attrs.appId) {
          local(console, 'error', 'appId not specified', 'RSKYBOX.log.isValid');
          return false;
        }
        if (!attrs.authHeader) {
          local(console, 'error', 'authHeader not specified', 'RSKYBOX.log.isValid');
          return false;
        }
        if (!attrs.logLevel) {
          local(console, 'error', 'logLevel not specified', 'RSKYBOX.log.isValid');
          return false;
        }
        if (!attrs.logName) {
          local(console, 'error', 'logName not specified', 'RSKYBOX.log.isValid');
          return false;
        }
        if (!attrs.message) {
          local(console, 'error', 'message not specified', 'RSKYBOX.log.isValid');
          return false;
        }

        return true;
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.isValid');
      }
    },


    // Create a new log on the rSkybox service.
    server = function (level, message, name) {
      try {
        var
          attrs = {
            appId: r.log.getApplicationId(),
            authHeader: r.log.getAuthHeader(),
            logName: name || message,
            logLevel: level,
            message: message,
            userName: r.log.getUserName(),
            summary: r.log.getSummary(),
            instanceUrl: r.log.getInstanceUrl(),
          };

        // Error level logs generall have an Error object as the message.
        // We'll just make sure it's not a string.
        if (level === 'error' && typeof attrs.message !== 'string') {
          attrs.message = 'see stackBackTrace';
          attrs.stackBackTrace = message.stack.split('\n');
        }

        // Ensure attrs are valid for making an Ajax call.
        if (!isValid(attrs)) { return; }

        $.ajax({
          type: 'POST',
          data: JSON.stringify(attrs),
          url: getUrl(),
          error: r.log.errorHandler,
          success: r.log.successHandler,
          statusCode: r.log.statusCodeHandlers,
          headers: {
            Authorization: r.log.getAuthHeader(),
          },
        });
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.server');
      }
    },


    // Traffic cop for determining where logs should go.
    base = function (level, message, name) {
      try {
        if (r.log.getApplicationId() && (r.log.getServerLevel() >= logLevels[level])) {
          server(level, message, name);
        }

        if (r.log.getLocalLevel() >= logLevels[level]) {
          local(r.log.getConsole(), level, message, name);
        }
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.base');
      }
    };
  // end var definitions


  r.log = {
    // Access to the apiCodes if the client app wants to user our messages.
    getApiCodes: function () {
      return apiCodes;
    },

    // Access to the log levels allowing the client to set server/local levels.
    getLevels: function () {
      return logLevels;
    },

    // Externalized logging methods for client app use.

    error: function (e, name) {
      base('error', e, name);
    },

    warn: function (message, name) {
      base('warn', message, name);
    },

    info: function (message, name) {
      base('info', message, name);
    },

    debug: function (message, name) {
      base('debug', message, name);
    },

    local: function (message, name) {
      base('local', message, name);
    },
  };


  return r;
}(RSKYBOX || {}, jQuery));
