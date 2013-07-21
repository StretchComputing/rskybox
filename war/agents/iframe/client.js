window.onload = function () {
  'use strict';


  RSKYBOX.rskyboxWindow = document.getElementById('rskybox').contentWindow,

  RSKYBOX.rskyboxConfig = {
    applicationName: '<%- app name goes here %>',
    applicationVersion: null,
    applicationId: '<%- app id goes here %>',
    authHeader: '<%- auth header goes here %>',
    userId: 'not set',
    userName: 'not set'
  };
};


var RSKYBOX = (function (r) {
  'use strict';


  var
    base = function (level, message, name) {
      r.rskyboxConfig.appActions = getAppActions(),
      r.rskyboxConfig.instanceUrl = window.location.hash;
      r.rskyboxConfig.summary = navigator.userAgent;

      r.rskyboxWindow.postMessage({
        level: level,
        data: {
          appConfig: r.rskyboxConfig,
          message: level !== 'error' ? message : { stack: message.stack },
          name: name
        }
      }, '*');
    },

    // AppActions that go along with a server log.
    appAction = {
      max: 20,
      first: 1,
      key: 'rAppAction',
      indexKey: 'rAppActionIndex',

      getIndex: function () {
        var index = +window.localStorage[this.indexKey] || this.first;

        if (index > this.max) {
          index = this.first;
        }
        window.localStorage[this.indexKey] = index + 1;
        return index;
      }
    },

    getAppActions = function () {
      var action, actions = [], i;

      for (i = appAction.first; i <= appAction.max; i += 1) {
        action = window.localStorage[appAction.key + i];
        if (action) {
          action = JSON.parse(action);
          action.timestamp = new Date(action.timestamp);
          actions.push(action);
        }
      }

      if (actions.length <= 0) {
        return;
      }

      actions.sort(function (a1, a2) {
        if (a1.timestamp < a2.timestamp) {
          return -1;
        }
        if (a1.timestamp > a2.timestamp) {
          return 1;
        }
        return 0;
      });

      for (i = 0; i < actions.length - 1; i += 1) {
        actions[i + 1].duration = actions[i + 1].timestamp - actions[i].timestamp;
      }
      actions[0].duration = -1;

      return actions;
    },

    saveAppAction = function (name, message) {
      var
        key = appAction.key + appAction.getIndex();

      window.localStorage[key] = JSON.stringify({
        description: name + ': ' + message,
        timestamp: new Date()
      });
    };


  r.log = {
    error: function (e, name) {
      base('error', e, name);
    },
    warn: function (message, name) {
      base('warn', message, name);
    },
    info: function (message, name) {
      // info logs are used to track appActions
      saveAppAction(name, message);
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
})(RSKYBOX || {});
