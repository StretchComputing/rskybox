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

    parse: function (response) {
      response.date = new Date(response.date);
      return response;
    },
  });


  r.Crashes = r.BaseCollection.extend({
    model: r.Crash,
    apiUrl: '/crashDetects',

    parse: function (response) {
      return response.crashDetects;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
