var RSKYBOX = (function (r, $) {
  'use strict';


  r.User = r.BaseModel.extend({
    apiUrl: '/users',

    initialize: function () {
      this.setUrl();
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
