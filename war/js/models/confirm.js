var RSKYBOX = (function (r, $) {
  'use strict';


  r.Confirm = r.BaseModel.extend({
    apiUrl: '/users',

    initialize: function () {
      this.setUrl();
    },

    fields: {
      emailAddress: null,
      emailConfirmationCode: null,
      phoneNumber: null,
      phoneConfirmationCode: null,
      password: null,
      new: null,
    },

    validate: function (attrs) {
      var
        PASSWORD_MIN_LEN = 6,
        CONFIRMATION_CODE_LEN = 3,
        emailCode,
        password,
        phoneCode,
        message = '';

      if (attrs.emailAddress && attrs.emailAddress.length > 0) {
        emailCode = attrs.emailConfirmationCode;
        if (emailCode) {
          if (emailCode.length === CONFIRMATION_CODE_LEN) {
            r.log.info('valid email confirmation code', 'Confirm.validate');
          } else {
            message += 'Confirmation code must be exactly 3 characters. ';
          }
        }
      }

      // Testing for presence of confirmation code error message.
      // So this test must follow the previous confirmation code test.
      if (!message && attrs.phoneNumber && attrs.phoneNumber.length > 0) {
        phoneCode = attrs.phoneConfirmationCode;
        if (phoneCode) {
          if (phoneCode.length === CONFIRMATION_CODE_LEN) {
            r.log.info('valid phone confirmation code', 'Confirm.validate');
          } else {
            message += 'Confirmation code must be exactly 3 characters. ';
          }
        }
      }

      if (!emailCode && !phoneCode) {
        message += 'Confirmation code is required. ';
      }

      password = attrs.password;
      r.log.info(attrs.new, 'Confirm.validate');
      if (!attrs.new || (password && password.length >= PASSWORD_MIN_LEN)) {
        r.log.info('valid password', 'Confirm.validate');
      } else {
        message += 'Password must be at least 6 characters. ';
      }

      if (message) { return message; }
    }
  });


  return r;
}(RSKYBOX || {}, jQuery));
