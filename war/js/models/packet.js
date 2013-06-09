var RSKYBOX = (function (r, $) {
  'use strict';


  r.Packet = r.BaseModel.extend({
    packetsUrl: '/packets',
    apiUrl: null,
    fields: {
      body: null
    }
  });


  r.Packets = r.BaseCollection.extend({
    model: r.Packet,
    packetsUrl: '/packets',
    apiUrl: null,

    parse: function (response) {
      try {
        console.log('parse****', response.packets);
        return response.packets;
      } catch (e) {
        r.log.error(e, 'Packets.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
