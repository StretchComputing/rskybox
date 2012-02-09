var RSKYBOX = (function (r, $) {
  'use strict';


  r.Signup = r.BaseModel.extend({
    apiUrl: '/users/requestConfirmation',
    fields: {
      emailAddress: null,
      phoneNumber: null,
      mobileCarrierId: null
    },

    initialize: function () {
      this.setUrl();
    },

    // We define parse and return nothing, because we don't need the model modifed after
    // a successful save.
    parse: function (response) {
      r.log.debug('Signup.parse');
    },

    validate: function (attrs) {
      if (r.isValidEmailAddress(attrs.emailAddress)) {
        r.log.debug('Signup: emailAddress is valid.');
        return;
      }
      if (r.isValidPhoneNumber(attrs.phoneNumber) && attrs.mobileCarrierId) {
        r.log.debug('Signup: phone credentials are valid.');
        return;
      }
      return 'A valid email address -OR- valid phone number and mobile carrier is required.';
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
