'use strict';


var rskybox = (function(r, $) {


  r.Signup = r.BaseModel.extend({
    apiUrl: '/users/requestConfirmation',

    initialize: function() {
      this.setUrl(this.apiUrl);
    },

    parse: function(response) {
      delete(response.apiStatus);
      return response;
    },

    validate: function(attrs) {
      r.log.debug('validate called');
      if (r.isValidEmailAddress(attrs.emailAddress)) {
        r.log.debug('emailAddress is valid');
        return;
      }
      if (r.isValidPhoneNumber(attrs.phoneNumber) && attrs.mobileCarrierId) {
        r.log.debug('phone credentials are valid');
        return;
      }
      return 'A valid email address -OR- valid phone number and mobile carrier is required.';
    }
  });


  return r;
})(rskybox || {}, jQuery);
