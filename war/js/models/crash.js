var RSKYBOX = (function (r, $) {
  'use strict';


  r.Crash = r.BaseModel.extend({
    apiUrl: '/crashDetects',
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


  r.Crashes = r.BaseCollection.extend({
    model: r.Crash,
    apiUrl: '/crashDetects',

    parse: function (response) {
      try {
        return response.crashDetects;
      } catch (e) {
        r.log.error(e, 'Crashes.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
