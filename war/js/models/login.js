var RSKYBOX = (function (r, $) {
  'use strict';


  r.Login = r.BaseModel.extend({
    apiUrl: '\/users\/token',

    initialize: function () {
      this.setUrl();
    },

    fields: {
      emailAddress: null,
      phoneNumber: null,
      password: null
    },

    getQueryObject: function () {
      return {
        userName: this.get('emailAddress') || this.get('phoneNumber') || '',
        password: this.get('password')
      };
    },

    validate: function (attrs) {
      var password, PASSWORD_MIN_LEN = 6;

      password = attrs.password;
      if ((password && password.length >= PASSWORD_MIN_LEN) &&
          (r.isValidEmailAddress(attrs.emailAddress) || r.isValidPhoneNumber(attrs.phoneNumber))) {
        r.log.debug('Login: credentials are valid.');
        return;
      }
      return 'A valid email address -OR- valid phone number, -AND- a valid password is required.';
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
