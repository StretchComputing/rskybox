var RSKYBOX = (function (r, $) {
  'use strict';


  r.Stream = r.BaseModel.extend({
    apiUrl: '/streams',
    packetsUrl: '/packets',
    fields: {
      id: null,
      appId: null,
      endUserId: null,
      memberId: null,
      createdDate: null,
      status: null,
      name: null
    }
  });


  r.Streams = r.BaseCollection.extend({
    model: r.Stream,
    apiUrl: '/streams',

    parse: function (response) {
      try {
        return response.streams;
      } catch (e) {
        r.log.error(e, 'Streams.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
