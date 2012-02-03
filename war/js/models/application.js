'use strict';


var rskybox = (function(r, $) {


  r.Application = Backbone.Model.extend({});


  r.Applications = r.BaseCollection.extend({
    model: r.Application,
    apiUrl: '/applications',

    initialize: function() {
      this.setUrl();
    },

    parse: function(response) {
      return response.applications;
    }
  });


  return r;
})(rskybox || {}, jQuery);
