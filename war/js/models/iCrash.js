var RSKYBOX = (function (r, $) {
  'use strict';


  r.Crash = r.BaseModel.extend({
    apiUrl: '/crashDetects',
    fields: {
      id: null,
      appId: null,
      summary: null,
      date: null,
      userName: null,
      instanceUrl: null,
      status: null,
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
