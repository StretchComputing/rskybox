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
      try {
        return response.endUsers;
      } catch (e) {
        r.log.error(e, 'Endusers.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
