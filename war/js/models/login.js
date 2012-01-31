'use strict';


var rskybox = (function(r, $) {


  r.Login = r.BaseModel.extend({
    apiUrl: '/users/token',

    initialize: function() {
      this.setUrl();
    },

    fields: {
      emailAddress: null,
      phoneNumber: null,
      password: null
    },

    getQueryObject: function() {
      return {
        userName: this.get('emailAddress') || this.get('phoneNumber') || '',
        password: this.get('password')
      };
    },

    // We define parse and return nothing, because we don't need the model modifed after
    // a successful save.
    parse: function(response) {
      r.log.debug('Confirm parse called.');
      r.dump(this);
    },

    validate: function(attrs) {
      var password, PASSWORD_MIN_LEN = 6;

      password = attrs.password;
      if ((password && password.length >= PASSWORD_MIN_LEN) &&
          (r.isValidEmailAddress(attrs.emailAddress) || r.isValidPhoneNumber(attrs.phoneNumber))) {
        r.log.debug('Login credentials are valid.');
        return;
      }
      return 'A valid email address -OR- valid phone number, -AND- a valid password is required.';
    }
  });


  return r;
})(rskybox || {}, jQuery);
