var RSKYBOX = (function (r, $) {
  'use strict';


  r.Member = r.BaseModel.extend({
    apiUrl: '/appMembers',
    fields: {
      id: null,
      appId: null,
      emailAddress: null,
      phoneNumber: null,
      date: null,
      role: null,
      status: null,
    },
  });


  r.Members = r.BaseCollection.extend({
    model: r.Member,
    apiUrl: '/appMembers',

    parse: function (response) {
      return response.appMembers;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
