var RSKYBOX = (function (r, $) {
  'use strict';


  r.Carrier = Backbone.Model.extend({});


  r.Carriers = r.BaseCollection.extend({
    model: r.Carrier,

    apiUrl: '/mobileCarriers',

    initialize: function () {
      this.setUrl();
    },

    parse: function (response) {
      return response.mobileCarriers;
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
