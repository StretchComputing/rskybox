'use strict';


var rskybox = (function(r, $) {


  r.User = r.BaseModel.extend({
    apiUrl: '/users',

    initialize: function() {
      this.setUrl();
    },
  });


  return r;
})(rskybox || {}, jQuery);
