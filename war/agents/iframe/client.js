var RSKYBOX = (function (r) {
  'use strict';


  var
    rskyboxWindow,
    rskyboxConfig = {
      applicationName: '<%- appName %>',
      applicationVersion: null,
      applicationId: '<%= appId %>',
      authHeader: '<%= authHeader %>',
      userId: 'not set',
      userName: 'not set'
    },


    base = function (level, message, name) {
      rskyboxConfig.appActions = getAppActions(),
      rskyboxConfig.instanceUrl = window.location.hash;
      rskyboxConfig.summary = navigator.userAgent;

      function later() {
        if (!RSKYBOX.IFRAME_READY) {
          window.setTimeout(later, 250);
          console.log('later');
          return;
        }
        rskyboxWindow = rskyboxWindow || document.getElementById('rskybox').contentWindow;
        rskyboxWindow.postMessage({
          level: level,
          data: {
            appConfig: rskyboxConfig,
            message: level !== 'error' ? message : { stack: message.stack },
            name: name
          }
        }, 'https://rskybox-stretchcom.appspot.com');
      }

      later();
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
    },

    receiveMessage = function (event) {
      if (event.data === 'ready') {
        RSKYBOX.IFRAME_READY = true;
      }
    };


  window.addEventListener('message', receiveMessage, false);

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
