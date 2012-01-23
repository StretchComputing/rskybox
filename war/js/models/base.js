'use strict';


var rskybox = (function(r, $) {


  r.BaseModel = Backbone.Model.extend({
    // The REST base portion of the URL, including the version.
    restUrl: '/rest/v1',

    // Set apiUrl when creating the concrete model then call makeUrl.
    setUrl: function(apiUrl) {
      if (apiUrl) {
        this.url = this.restUrl + this.apiUrl;
      } else {
        r.log.error('invalid apiUrl: ' + apiUrl);
        this.url = '';
      }
    }
  });


  return r;
})(rskybox || {}, jQuery);
