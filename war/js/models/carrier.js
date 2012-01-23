'use strict';


var rskybox = (function(r, $) {


  r.Carrier = Backbone.Model.extend({});


  r.Carriers = r.BaseCollection.extend({
    model: r.Carrier,

    apiUrl: '/mobileCarriers',

    initalize: function() {
      this.setUrl(this.apiUrl);
    },

    parse: function(response) {
      return response.mobileCarriers;
    }
  });


  return r;
})(rskybox || {}, jQuery);
