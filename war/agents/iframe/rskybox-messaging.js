//
// rSkybox JavaScript Agent, v0.1 Beta, 2012/05/02 14:00 EDT
//
// Copyright © 2012 by Stretch Computing, Inc. All rights reserved.
//
// Any redistribution or reproduction of part or all of the contents in any
// form is prohibited without prior approval. You may not, except with our
// express written permission, distribute or commercially exploit the content.
//

var RSKYBOX = (function (r) {
  'use strict';

  var
    source = null,

    receiveMessage = function (event) {
      source = event.source;

      switch (event.data.message) {
      case 'init':
        r.rskyboxConfig = event.data.data;
        break;
      case 'error':
        r.log.error(event.data.data, 'rskybox-messaging.js');
        break;
      default:
        window.console.log('unknown message received');
        break;
      }
    };

  r.rskyboxConfig = {
    applicationName: null,
    applicationVersion: null,
    applicationId: null,
    authHeader: null,
    userId: 'not set',
    userName: 'not set'
  };

  window.addEventListener('message', receiveMessage, false);

}(RSKYBOX || {}));
