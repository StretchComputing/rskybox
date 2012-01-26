'use strict';


var rskybox = (function(r, $) {


  r.Confirm = r.BaseModel.extend({
    apiUrl: '/users/confirm',

    initialize: function() {
      this.setUrl(this.apiUrl);
    },

    validate: function(attrs) {
      var
        PASSWORD_MIN_LEN = 6,
        CONFIRMATION_CODE_LEN = 3
        code,
        password,
        message = '';

      r.log.debug('validate called');

      code = attrs.confirmationCode;
      if (code && code.length == CONFIRMATION_CODE_LEN) {
        r.log.debug('confirmation code is valid');
      } else {
        message += 'Confirmation code is 3 characters. ';
      }

      password = attrs.password;
      if (password && password.length >= PASSWORD_MIN_LEN) {
        r.log.debug('password is valid');
      } else {
        message += 'Password must be at least 6 characters. ';
      }

      if (message) { return message; }
    }
  });


  return r;
})(rskybox || {}, jQuery);
