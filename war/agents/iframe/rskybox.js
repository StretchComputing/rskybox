//
// rSkybox JavaScript Agent, v0.1 Beta, 2012/05/02 14:00 EDT
//
// Copyright © 2012 by Stretch Computing, Inc. All rights reserved.
//
// Any redistribution or reproduction of part or all of the contents in any
// form is prohibited without prior approval. You may not, except with our
// express written permission, distribute or commercially exploit the content.
//

var RSKYBOX = (function (r, $) {
  'use strict';


  r.restUrlBase = '/rest/v1';

  var
    logLevels = {
      off: 0,
      error: 5,
      warn: 10,
      info: 25,
      debug: 50,
      local: 75
    },

    apiCodes = {
      202: 'Invalid log level.',
      305: 'Application ID required.',
      315: 'Log name is required.',
      414: 'Invalid timestamp parameter.',
      415: 'Invalid duration parameter.',
      605: 'Application not found.'
    },


    // The URL for the REST call to create an rSkybox log.
    getUrl = function () {
      try {
        return r.restUrlBase + '/applications/' + r.config.getApplicationId() + '/clientLogs';
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.getUrl');
      }
    },


    // Log information at the console object provided.
    local = function (console, level, message, name) {
      try {
        var output;

        // Not defining the console turns off console logging.
        // Set the console object in the r.config.getConsole() method.
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


    // Make sure we have valid attributes for logging to the server.
    isValid = function (attrs) {
      try {
        var console = r.config.getConsole();

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


    // Disallow multiple logs being created at once.
    guard = (function () {
      var guarding = false;

      return {
        enable: function () {
          guarding = true;
        },

        disable: function () {
          guarding = false;
        },

        isEnabled: function () {
          return guarding;
        }
      };
    }()),


    // Create a new log on the rSkybox service.
    server = function (level, message, name) {
      try {
        var attrs;

        if (guard.isEnabled()) {
          r.log.local('not logging, guard on', 'RSKYBOX.log.server');
          return;
        }
        r.log.local('logging, guard off', 'RSKYBOX.log.server');
        guard.enable();

        attrs = {
          appId: r.config.getApplicationId(),
          authHeader: r.config.getAuthHeader(),
          logName: name || message,
          logLevel: level,
          message: message,
          userId: r.config.getUserId(),
          userName: r.config.getUserName(),
          summary: r.config.getSummary(),
          localEndpoint: r.config.getLocalEndpoint(),
          remoteEndpoint: r.config.getRemoteEndpoint(),
          appActions: r.config.getAppActions()
        };

        // Error level logs generally have an Error object as the message.
        // We'll just make sure it's not a string.
        if (level === 'error' && typeof attrs.message !== 'string') {
          attrs.message = 'see stackBackTrace';
          attrs.stackBackTrace = message.stack.split('\n');
          attrs.logName = (function (arr) {
            var
              i,
              re = /http:\/\/[^:]*:*\d*/i,
              result;

            for (i = 0; i <= 1; i += 1) {
              result = re.exec(arr[i]);
              if (result && result[0]) {
                return result[0];
              }
            }

            return attrs.logName;
          })(attrs.stackBackTrace);
        }

        // Ensure attrs are valid for making an Ajax call.
        if (!isValid(attrs)) { return; }

        delete attrs.appId;
        delete attrs.authHeader;

        $.ajax({
          type: 'POST',
          data: JSON.stringify(attrs),
          url: getUrl(),
          error: function (jqXHR, textStatus, errorThrown) {
            try {
              guard.enable();
              r.config.log.errorHandler(jqXHR, textStatus, errorThrown);
              guard.disable();
            } catch (e) {
              window.console.error(e, 'RSKYBOX.log.server.errorHandler');
            }
          },
          success: function (data, status, jqXHR) {
            try {
              guard.enable();
              r.config.log.successHandler(data, status, jqXHR);
              guard.disable();
            } catch (e) {
              window.console.error(e, 'RSKYBOX.log.server.successHandler');
            }
          },
          statusCode: r.config.log.statusCodeHandlers,
          headers: {
            Authorization: r.config.getAuthHeader()
          }
        });

        guard.disable();
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.server');
      }
    },


    // Traffic cop for determining where logs should go.
    base = function (level, message, name) {
      try {
        if (r.config.getApplicationId() && (logLevels[level] <= r.config.log.getServerLevel())) {
          server(level, message, name);
        }

        if (logLevels[level] <= r.config.log.getLocalLevel()) {
          local(r.config.getConsole(), level, message, name);
        }
      } catch (e) {
        window.console.error(e, 'RSKYBOX.log.base');
      }
    };
  // end var definitions


  r.log = {
    // Access to the apiCodes if the client app wants to use our messages.
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
    }
  };


  return r;
}(RSKYBOX || {}, jQuery));
