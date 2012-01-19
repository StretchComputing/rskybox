'use strict';


var rskybox = (function(r, $) {

  r.UnconfirmedUser = Backbone.Model.extend({
    url: r.getRestPrefix() + '/users/requestConfirmation',
    initialize: function() {
      this.bind('error', this.handleError);
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
    handleError: function(model, errors) {
      console.log(model, errors);
    },
    validate: function(attrs) {
      if (r.isValidEmailAddress(attrs.emailAddress)) { return; }
      if (r.isValidPhoneNumber(attrs.phoneNumber) && attrs.mobileCarrierId) { return; }

      return 'Required: valid email address -OR- valid phone number and mobile carrier';
    },
    warnings: {
      api204: 'Your email address has already been confirmed',
      api205: 'Your phone number has already been confirmed'
    }
  });


  return r;
})(rskybox || {}, jQuery);
