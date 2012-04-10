var RSKYBOX = (function (r, $) {
  'use strict';


  r.Carrier = Backbone.Model.extend({});


  r.Carriers = r.BaseCollection.extend({
    model: r.Carrier,

    apiUrl: '/mobileCarriers',

    initialize: function () {
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'Carriers.initialize');
      }
    },

    parse: function (response) {
      try {
        return response.mobileCarriers;
      } catch (e) {
        r.log.error(e, 'Carriers.parse');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
