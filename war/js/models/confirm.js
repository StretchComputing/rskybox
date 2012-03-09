var RSKYBOX = (function (r, $) {
  'use strict';


  r.Confirm = r.BaseModel.extend({
    apiUrl: '/users',

    initialize: function () {
      this.setUrl();
    },

    fields: {
      emailAddress: null,
      phoneNumber: null,
      confirmationCode: null,
      password: null,
      new: null,
    },

    validate: function (attrs) {
      var
        PASSWORD_MIN_LEN = 6,
        CONFIRMATION_CODE_LEN = 3,
        code,
        password,
        message = '';

      code = attrs.confirmationCode;
      if (code && code.length === CONFIRMATION_CODE_LEN) {
        r.log.debug('valid confirmation code', 'Confirm.validate');
      } else {
        message += 'Confirmation code must be exactly 3 characters. ';
      }

      password = attrs.password;
      r.log.debug(attrs.new, 'Confirm.validate');
      if (!attrs.new || (password && password.length >= PASSWORD_MIN_LEN)) {
        r.log.debug('valid password', 'Confirm.validate');
      } else {
        message += 'Password must be at least 6 characters. ';
      }

      if (message) { return message; }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
