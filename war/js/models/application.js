'use strict';


var rskybox = (function(r, $) {


  r.Application = r.BaseModel.extend({
    apiUrl: '/applications',
    fields: {
      id: null,
      name: null,
      version: null,
      date: null,
      token: null
    },

    initialize: function() {
      this.setUrl();
    },

    validate: function(attrs) {
      if (attrs.name) {
        return;
      }
      return 'A name is required for your application.';
    }
  });


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
}(rskybox || {}, jQuery));
