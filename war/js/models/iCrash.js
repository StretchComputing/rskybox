var RSKYBOX = (function (r, $) {
  'use strict';


  r.iCrash = r.BaseModel.extend({
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
      eventCount: null,
      summary: null,
      mode: null,
      message: null,
      appActions: null,
    },
  });


  r.iCrashes = r.BaseCollection.extend({
    model: r.iCrash,
    apiUrl: '/incidents?tag=crash',

    parse: function (response) {
      try {
        return response.incidents;
      } catch (e) {
        r.log.error(e, 'iCrashes.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
