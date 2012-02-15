var RSKYBOX = (function (r, $) {
  'use strict';


  r.Enduser = r.BaseModel.extend({
    apiUrl: '/endUsers',
    fields: {
      id: null,
      appId: null,
      userName: null,
      application: null,
      version: null,
      instanceUrl: null,
    },
  });


  r.Endusers = r.BaseCollection.extend({
    model: r.Enduser,
    apiUrl: '/endUsers',

    parse: function (response) {
      return response.endUsers;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
