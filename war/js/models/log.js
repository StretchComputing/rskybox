var RSKYBOX = (function (r, $) {
  'use strict';


  r.Log = r.BaseModel.extend({
    apiUrl: '/clientLogs',
    fields: {
      id: null,
      appId: null,
      logName: null,
      logMode: null,
      date: null,
      userName: null,
      instanceUrl: null,
      logLevel: null,
      message: null,
      stackBackTrace: null,
      status: null,
      appActions: null,
    },

    parse: function (response) {
      response.stackBackTrace = JSON.parse(response.stackBackTrace);
      return response;
    },
  });


  r.Logs = r.BaseCollection.extend({
    model: r.Log,
    apiUrl: '/clientLogs',

    parse: function (response) {
      try {
        return response.clientLogs;
      } catch (e) {
        r.log.error(e, 'Logs.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
