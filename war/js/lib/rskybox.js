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

    isValid = function () {
      try {
        var valid = true;

        return valid;
      } catch (e) {
        r.log.error(e, 'RSKYBOX..log.isValid');
      }
    },

    // message is an Error object for 'error' level
    local = function (console, level, message, name) {
      try {
        var output;

        if (level !== 'error') {
          output = message + (name ? ' \t(' + name + ')' : '');
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

    // message is an Error object for 'error' level
    // TODO - log to localStorage when it's not possible to log to the server
    server = function (level, message, name) {
      if (!isValid()) { throw new Error('log setup is invalid'); }

      try {
        var
          attrs = {
            instanceUrl: r.log.getInstanceUrl(),
            logName: name || message,
            logLevel: level,
            message: message,
            userName: r.log.getUserName(),
          };

        if (level === 'error') {
          attrs.message = 'see stackBackTrace';
          attrs.stackBackTrace = message.stack.split('\n');
        }

        $.ajax({
          type: 'POST',
          data: JSON.stringify(attrs),
          url: getUrl(),
          error: r.log.errorHandler(),
          success: r.log.successHandler(),
          statusCode: r.log.statusCondeHandlers(),
          headers: {
            Authorization: r.log.getAuthHeader(),
          },
        });
      } catch (e) {
        r.log.error(e, 'rSkyboxLog.logToServer');
      }
    },

    base = function (level, message, name) {
      try {
        if (r.log.getApplicationId() && (r.log.getServerLevel() >= logLevels[level])) {
          server(level, message, name);
        }

        if (r.log.getConsole() && r.log.getLocalLevel() >= logLevels[level]) {
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
