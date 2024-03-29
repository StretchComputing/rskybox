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
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'Signup.initialize');
      }
    },

    parse: function (response) {
      try {
        r.log.info('model not needed after successful save', 'Signup.parse');
      } catch (e) {
        r.log.error(e, 'Signup.parse');
      }
    },

    validate: function (attrs) {
      try {
        if (r.isValidEmailAddress(attrs.emailAddress)) {
          r.log.info('valid emailAddress', 'Signup.validate');
          return;
        }
        if (r.isValidPhoneNumber(attrs.phoneNumber) && attrs.mobileCarrierId) {
          r.log.info('valid phone credentials', 'Signup.validate');
          return;
        }
        return 'A valid email address -OR- valid phone number and mobile carrier is required.';
      } catch (e) {
        r.log.error(e, 'Signup.validate');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
