var RSKYBOX = (function (r, $) {
  'use strict';


  var
    urlRoot = 'https://rskybox-stretchcom.appspot.com/rest/v1/applications/<appId>/clientLogs',

    logLevels = {
      error: 5,
      warn: 10,
      info: 25,
      debug: 50,
      local: 75,
      off: 99
    },

    settings = {
      appId: undefined,
      token: undefined,
      serverLevel: logLevels.error,
      localLevel: logLevels.local,
      summary: (function () {
        try {
          // TODO - include environment information in summary
          // TODO - include error name in summary
          return 'placeholder summary';
        } catch (e) {
          console.error(e, 'RSKYBOX.log.settings.summary');
        }
      }()),
    },

    isValid = function () {
      try {
        var valid = false;

        return valid;
      } catch (e) {
        console.error(e, 'RSKYBOX..log.isValid');
      }
    },

    // message is an Error object for 'error' level
    local = function (level, message, name) {
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
        // TODO - log to localStorage
        console.error(e, 'rSkyboxLog.log.local');
      }
    },

    // message is an Error object for 'error' level
    server = function (level, message, name) {
      try {
        if (!isValid) { return; }

        var
          attrs = {
            instanceUrl: location.hash,
            logName: name || message,
            logLevel: level,
            message: message,
            userName: Cookie.get('token'),
          },
          getUserName,
          user;

        if (level === 'error') {
          attrs.message = 'see stackBackTrace';
          attrs.stackBackTrace = message.stack.split('\n');
        }

        this.save(attrs, {
          error: settings.error,
          success: settings.success,
          statusCode: settings.statusCode,
        });
      } catch (e) {
        // TODO - log to localStorage
        console.error(e, 'rSkyboxLog.logToServer');
      }
    },

    apiCodes = {
      202: 'Invalid log level.',
      305: 'Application ID required.',
      315: 'Log name is required.',
      414: 'Invalid timestamp parameter.',
      415: 'Invalid duration parameter.',
      605: 'Application not found.',
    };


  r.log = {
    initialize: function (options) {
      try {
        if (!options) { return; }

        Object.keys(settings).forEach(function (key) {
          if (options[key]) {
            settings[key] = options[key];
          }
        });
      } catch (e) {
        // TODO - log to localStorage
        console.error(e, 'RSKYBOX.log.initialize');
      }
      return this.isValid();
    },

    isValid: function () {
      return isValid();
    },

    getApiCodes: function () {
      return apiCodes;
    },

    getLogLevels: function () {
      return logLevels;
    },

    error: function (e, name) {
      this.base('error', e, name);
    },

    warn: function (message, name) {
      this.base('warn', message, name);
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
          localLevel = logLevels.local,
          serverLevel = logLevels.error;

        if (this.get('appId') && (serverLevel >= logLevels[level])) {
          server(level, message, name);
        }

        if (localLevel >= logLevels[level]) {
          local(level, message, name);
        }
      } catch (e) {
        // TODO - log to localStorage
        console.error(e, 'rSkyboxLog.base');
      }
    },
  }

  return r;
}(RSKYBOX || {}, jQuery));
