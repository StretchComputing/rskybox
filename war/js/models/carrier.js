'use strict';


var rskybox = (function(r, $) {


  r.Carrier = Backbone.Model.extend({});


  r.Carriers = r.BaseCollection.extend({
    model: r.Carrier,

    apiUrl: '/mobileCarriers',

    initialize: function() {
      this.setUrl(this.apiUrl);
    },

    parse: function(response) {
      if (+response.apiStatus !== 100) {
        r.displayWarning('Unknown API status: ' + response.apiStatus);
        return;
      }
      return response.mobileCarriers;
    }
  });


  return r;
})(rskybox || {}, jQuery);
