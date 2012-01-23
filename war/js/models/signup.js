'use strict';


var rskybox = (function(r, $) {


  r.Signup = r.BaseModel.extend({
    apiUrl: '/users/requestConfirmation',

    initialize: function() {
      this.setUrl(this.apiUrl);
      //_.bindAll(this, 'handleSuccess');
      //this.bind('error', this.handleError, this);
    },

    parse: function(response) {
      switch(+response.apiStatus) {
        case 100:
          delete(response.apiStatus);
          return response;
        case 204: r.displayWarning(this.warnings.api204); return;
        case 205: r.displayWarning(this.warnings.api205); return;
        default: r.displayWarning('Unknown error occurred for apiStatus: ' + response.apiStatus); return;
      }
    },

    //handleError: function(model, errors) {
      //r.log.debug(errors);
    //},

    //handleSuccess: function(model, response) {
      //r.log.debug('handleSuccess');
    //},

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

      r.log.debug('validate returning string');
      return 'A valid email address -OR- valid phone number and mobile carrier is required.';
    },

    warnings: {
      api204: 'Your email address has already been confirmed',
      api205: 'Your phone number has already been confirmed'
    }
  });


  return r;
})(rskybox || {}, jQuery);
