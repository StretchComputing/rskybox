var RSKYBOX = (function (r, $) {
  'use strict';


  r.Login = r.BaseModel.extend({
    apiUrl: '/users/token',

    initialize: function () {
      try {
        this.setUrl();
      } catch (e) {
        r.log.error(e, 'Login.initialize');
      }
    },

    fields: {
      emailAddress: null,
      phoneNumber: null,
      password: null
    },

    getQueryObject: function () {
      try {
        return {
          userName: this.get('emailAddress') || this.get('phoneNumber') || '',
          password: this.get('password')
        };
      } catch (e) {
        r.log.error(e, 'Login.getQueryObject');
      }
    },

    validate: function (attrs) {
      try {
        var password, PASSWORD_MIN_LEN = 6;

        password = attrs.password;
        if ((password && password.length >= PASSWORD_MIN_LEN) &&
            (r.isValidEmailAddress(attrs.emailAddress) || r.isValidPhoneNumber(attrs.phoneNumber))) {
          r.log.info('credentials are valid', 'Login.validate');
          return;
        }
        return 'A valid email address -OR- valid phone number, -AND- a valid password is required.';
      } catch (e) {
        r.log.error(e, 'Login.validate');
      }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
