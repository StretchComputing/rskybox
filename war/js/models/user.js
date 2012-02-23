var RSKYBOX = (function (r, $) {
  'use strict';


  r.User = r.BaseModel.extend({
    apiUrl: '/users',
    fields: {
      id: null,
      firstName: null,
      lastName: null,
      emailAddress: null,
      phoneNumber: null,
      mobileCarrierId: null,
      sendEmailNotifications: null,
      sendSmsNotifications: null,
      isSuperAdmin: null,
    },

    initialize: function () {
      this.setUrl();
    },
  });


  return r;
}(RSKYBOX || {}, jQuery));
