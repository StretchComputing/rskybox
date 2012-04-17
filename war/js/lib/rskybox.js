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

    getUrl = function () {
      try {
        //return 'https://rskybox-stretchcom.appspot.com/rest/v1/applications/' + settings.appId + '/clientLogs';
        return '/rest/v1/applications/' + r.log.getApplicationId() + '/clientLogs';
      } catch (e) {
        r.log.error(e, 'RSKYBOX.log.getUrl');
      }
    },

    // message is an Error object for 'error' level
    local = function (console, level, message, name) {
      try {
        var output;

        if (!console) { return; }

        if (typeof message === 'string') {
          output = message + (name ? ' \t(' + name + ')' : '');
        } else {
          output = (name ? name + ' \t' : '') + message.stack;
        }

        switch (level) {
        case 'error':
          console.error(name, message.stack);
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
        r.log.error(e, 'RSKYBOX.log.local');
      }
    },

    cache = function (level, message, name) {
      // TODO - need functionality to log to localStorage
      local(r.log.getConsole(), level, message, name);
    },

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
        cache('error', e, 'RSKYBOX.log.isValid');
      }
    },

    // message is an Error object for 'error' level
    // TODO - log to localStorage when it's not possible to log to the server
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

        if (level === 'error') {
          attrs.message = 'see stackBackTrace';
          attrs.stackBackTrace = message.stack.split('\n');
        }

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
        local(r.log.getConsole(), 'error', e, 'RSKYBOX.log.server');
      }
    },

    base = function (level, message, name) {
      try {
        if (r.log.getApplicationId() && (r.log.getServerLevel() >= logLevels[level])) {
          server(level, message, name);
        }

        if (r.log.getLocalLevel() >= logLevels[level]) {
          local(r.log.getConsole(), level, message, name);
        }
      } catch (e) {
        r.log.error(e, 'RSKYBOX.log.base');
      }
    };
  // end var definitions


  r.log = {
    isValid: function () {
      return isValid();
    },

    getApiCodes: function () {
      return apiCodes;
    },

    getLevels: function () {
      return logLevels;
    },

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

