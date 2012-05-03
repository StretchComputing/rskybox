var RSKYBOX = (function (r, $) {
  'use strict';


  r.iLog = r.BaseModel.extend({
    apiUrl: '/incidents',
    fields: {
      id: null,
      appId: null,
      number: null,
      status: null,
      severity: null,
      name: null,
      lastUpdatedDate: null,
      createdDate: null,
      tags: null,
      firstEvent: null,
      eventCount: null,
      summary: null,
      mode: null,
      message: null,
      appActions: null,
    },
  });


  r.iLogs = r.BaseCollection.extend({
    model: r.iLog,
    apiUrl: '/incidents?tag=log',

    parse: function (response) {
      try {
        return response.incidents;
      } catch (e) {
        r.log.error(e, 'iLogs.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
